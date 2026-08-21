# BeanInfo 二进制格式 v2（草案）

> 目标：单个类的类型信息（properties / ctors / methods）的紧凑、自包含、可随机读取的二进制表示。
> 设计借鉴 class file 与 TASTy：**自包含常量池 + 长度前缀段 + magic/版本**；类型内容来自**编译期 digger**
> （与 TASTy 信息等价，但格式完全自控，运行期不依赖任何编译器/工具链库）。
> 单位 blob 自包含（不引用外部），为将来 jar 级 `beaninfo.idx`（目录 = 类名 → offset/length）就绪。

## 1. 总览

```
<SimpleName>.beaninfo            （与 .class 同包同目录；旧 v1 为 <SimpleName>.beaninfo.json）

┌───────────────────────────────────────────────┐
│ 头部：magic "BNI2" + version u16 + flags u16   │  10B
│        + nameIdx u16（本类名，池索引）          │
├───────────────────────────────────────────────┤
│ 常量池：poolSize u16 + 若干 UTF8 条目            │  全部字符串去重，其余结构按索引引用
├───────────────────────────────────────────────┤
│ 段（可多个，顺序无关，未知段按 length 跳过）：    │
│   tag=1 properties │ tag=2 ctors │ tag=3 methods│
└───────────────────────────────────────────────┘
```

- 所有多字节整数**大端序**（与 class file 一致）。选择理由：**大端 = TCP/IP 网络字节序 + Java 规范默认（DataOutputStream/ByteBuffer/class 文件 u2u4）**——JVM 生态读写零转换、跨平台传输对齐网络惯例；小端阵营（x86 主机内部、USB/PCI 硬件总线）与元数据格式场景无关。§4 的 Form 1 bit7 标记方案亦只有大端下成立（小端首字节低字节 0x80+ 会撞标记）；
- 池索引为 u16，**0xFFFF = 无（absent）**；
- 类名用 JVM 内部名（`/` 分隔）：`org/beangle/data/hibernate/model/User`，加载时 `/`→`.` 再 `Class.forName`；
- 基本类型用 JVM 名：`int` `long` `short` `boolean` `byte` `char` `float` `double` `void`（`classFor` 映射回 primitive Class）。

## 2. 头部（10B）

| 字段 | 大小 | 说明 |
|---|---|---|
| magic | 4B | `"BNI2"`（0x42 0x4E 0x49 0x32） |
| version | u16 | = 2 |
| flags | u16 | 保留，= 0 |
| nameIdx | u16 | 本类内部名在常量池中的索引（自描述/校验用） |

## 3. 常量池

```
poolSize: u16                        // 显式条目数，索引 128 .. 128+poolSize-1
poolSize × { tag: u8, len: u16, bytes: UTF-8 }
```

- **内置类型区（固定索引 0–127，0 字节）**：primitive + `java/lang/String` + `scala/Option` + 装箱类型（索引 0–17）+ 常用日期/时间/日历类型（索引 18–38：`java/util/Date`、`java/sql/Date`、`java/sql/Timestamp`、`java/sql/Time`、`java/util/Calendar`、`GregorianCalendar`、`TimeZone`、`Locale`、`java/time/*`：LocalDate/LocalDateTime/LocalTime/Instant/ZonedDateTime/OffsetDateTime/Duration/Period/YearMonth/Year/MonthDay/Month/DayOfWeek）+ Scala 集合与 java.util 集合（索引 39–66：`scala/collection/immutable/`List/Seq/Set/Map/Vector/IndexedSeq/HashSet/HashMap、`scala/collection/mutable/`Map/Set/Buffer/Seq/ArrayBuffer/ListBuffer/HashMap/HashSet、`scala/collection/`Seq/Set/Map/Iterable/IndexedSeq、`java/util/`List/Set/Map/Collection/ArrayList/HashSet/HashMap）+ beangle 内建值类型（索引 67–72：`org/beangle/commons/lang/time/`WeekTime/HourMinute/CycleTime/WeekDay、`org/beangle/commons/lang/math/`Decimal5/TinyDecimal5）+ 其他常用数据模型类型（索引 73–79：`java/math/`BigDecimal/BigInteger、`java/util/UUID`、`java/lang/Object`、`scala/math/`BigDecimal/BigInt、`java/lang/Number`）+ `java/util/Properties` 与 commons Json 值类型（索引 80–84：`org/beangle/commons/json/`Json/JsonObject/JsonArray/JsonValue）——全部**不进池**；85–127 **保留**供未来内置名（加内置名不动显式边界 → 已生成文件依然可读）；引用解析：`idx<85 → 内置表`、`85≤idx<128 → 格式错误（未启用保留索引）`；显式条目从索引 **128** 起（`nameIdx` 与所有字符串引用按同一规则解析）。每 blob 省 ~40–80B 池开销；内置区增删会改变索引空间 → 结构性变更走版本号；
- **tag = 条目类型标记**（类比 JVM class file 的 `CONSTANT_Utf8`/`CONSTANT_Integer` 等）：解码器按 tag 决定如何解释条目数据。目前仅定义 tag=0（UTF-8 字符串，即类名/属性名/方法名），其余值保留供未来扩展（如整数池、类型池）；
- 与段（§5，未知 tag 按 length 跳过）不同，池条目**严格校验**：解码器读到 tag≠0 直接抛错（池是全部索引引用的基础，无法跳过，结构性扩展走版本号）；
- **len 为什么是 u16**：字符串变长，解码器须靠长度推进到下一条目（`poolSize` 只给条数、不给边界）；u16 上限 65535 与 JVM `CONSTANT_Utf8` 的 u2 length 一致，恰好覆盖 JVM 类名/方法名最大长度——u8（255）对合法类名不够，NUL 结尾不标准且需逐字节扫描。唯一理论边界：ctor 默认值的 String 不受 JVM 名字长度限制，超 65535B 会溢出（编码侧可加守卫）；
- 属性名与 getter 名相同（Scala 常见）时是**同一池索引**，天然去重。

## 4. TypeInfo（双形态，无类型标记）

```
Form 1（内置 clazz < 128 且 argCount < 128）：[1B: bit7=1 | argCount(7bit)] [1B clazzIdx]
Form 2（显式 clazz / argCount ≥ 128）：[2B clazzIdx u16] [1B argCount]
均为：argCount × TypeInfo（递归）
```

- **大端下 Form 1 标记不冲突**：Form 2 首字节即 u16 高字节，显式索引 < 0x8000（编码校验）时高字节 0x00–0x7F、bit7 恒 0，与 Form 1 的 bit7=1 零重叠——该"首字节 bit7 做标记"方案**只有在大端下成立**（小端下低字节 0x80–0xFF 会撞标记）；
- 内置类型（§3 表）typeinfo 从 3B → **2B**，占绝大多数（String/long/集合/日期…）；argCount 折叠进标记字节低 7 位（实际 0–3）；
- **无 option/iterable/general 区分**：三种 kind 一律压平成 `clazz + args`——`Option[X]` 的 clazz 即 `scala/Option`、args=[X]；`IterableType` 同；自定义泛型（`Wrapper[Foo]`）也同；
- **kind 在读取时经 `TypeInfo.get(clazz, args)` 还原**：clazz==scala/Option → `OptionType`、`TypeInfo.isIterableType(clazz)` → `IterableType`、否则 `GeneralType`——无需二进制标记；
- **属性级 Option 特例**：属性的类型为 `Option[X]` 时，存储**元素类型 X**（压平），并在属性 flags 的 bit1 记 isOptional（§5.1）——`scala/Option` 不入池；构造时 `if isOptional then OptionType(typeinfo) else typeinfo`。构造器参数与嵌套泛型中的 `Option[X]` 仍按 `scala/Option + [X]` 存储（无属性级标志可用）。

## 5. 段

每段：`tag u8 + length u32 + payload`；未知 tag 按 length 跳过（前向兼容）。

- **length 为什么是 u32**（区别于池条目的 u16）：payload 是复合数据（count + 多条记录），大类的属性表可能超过 64KB，u16 会封顶；且未知段要按 length 整体跳过，长度必须完整给出。u32 上限约 4GB，对单个类 blob 实际无界。

### 5.1 properties（tag=1）

```
payload: count u8, count × {
  nameIdx           u16   属性名（池索引，pool[nameIdx] 解出字符串）
  typeinfo          TypeInfo
  flags             u8    bit0=transient；bit1=isOptional（属性类型为 Option 时置位，typeinfo 存元素类型，§4）；其余保留
}
```

- **纯声明记录，无任何访问器信息**：getter/setter 方法名与参数类型都不存储——构造 BeanInfo 时按命名约定发现 accessor（`getPropertyName` 处理 Java bean 的 `getX`/`setX` 与 Scala 的 `x`/`x_$eq`，§6），Java 兼容由构造路径保证，不依赖二进制存名；
- **setter 参数类型不存储**：标准 JavaBean/Scala `var` 的 setter 参数擦除类恒等于 `typeinfo.clazz`（如 `nick: Option[String]` → `nick_$eq(scala.Option)`），构造 BeanInfo 时用 `typeinfo.clazz` 匹配即可（§6）。

> **为什么不存声明类**：加载器在**本类**上经 `getMethods`（继承解析，含父类/接口方法；对非 public 类同样有效）按 name+参数类型匹配重建 getter/setter——与编译期 dig 的 `BeanInfo.Builder` 同一查找语义，故无需存储声明类。继承属性（`ExtendRole.id` 的 getter 在 `Role`、`CodecSample.base` 的 getter 在 `CodecBase`）天然可解析；编译器生成的 bridge（泛型 trait + primitive 实现，如 `Entity[Int]` 的 `id(): Object`）与本类真实方法同名同参，靠"优先非 bridge"区分；public 方法从包私有父类继承时，JVM 在公开子类合成转发 bridge，仍可调用。每属性省 2×u16。

### 5.2 ctors（tag=2）

```
payload: count u8, count × {
  paramCount u8,
  paramCount × {
    nameIdx     u16   参数名（池索引）
    typeinfo    TypeInfo
    defaultTag  u8, 按 tag 携带值：
      0  = 无
      1  = null
      2  = boolean (1B)
      3  = byte    (1B)
      4  = short   (2B)
      5  = int     (4B)
      6  = long    (8B)
      7  = float   (4B IEEE)
      8  = double  (8B IEEE)
      9  = char    (2B)
     10  = string  (u16 utf8 池索引)
     11  = enum    (u16 常量名池索引；加载时 Enum.valueOf(clazz, name))
     >11 = 不支持 → 序列化为 0（丢弃值并告警）
  }
}
```

### 5.3 methods（tag=3）

```
payload: count u16, count × {
  nameIdx       u16   方法名（池索引）
  paramCount    u8
  paramCount × paramTypeIdx u16    // 参数擦除类名（池索引）
}
```

（与 v1 JSON 的 {declaring, name, params} 同构，仅少了声明类——方法在本类经 `getMethods` 按 name+参数类型重建。内容来自 dig 的 `Builder.build()`/`BeanInfoLoader` 反射收集（擦除签名，无编译期精度）；`parse` 阶段以原始字符串保留在 `MetaModel.Method`，构造 BeanInfo 时（未来）才解析成 Method。编译期 dig 产物此段常为 count=0。**count 保持 u16**——大型类非属性方法数可能超 255，留余量；其余计数（属性/构造器/参数/argCount）为 u8，上限 255 编码时校验。）

### 5.4 beaninfo.idx（多类容器 + 目录，MetaModelIndex）

多个类的 blob 拼接成一个 `beaninfo.idx` 文件，配目录按类名定位（jar 级索引，避免散落大量 `.beaninfo`；native-image 只注册一个资源模式）：

```
header:    magic "BNIX" + version u16
directory: count u32, count × { nameLen u16 + name UTF-8 + offset u32 + length u32 }
blobs:     count × { 自包含 v2 blob（= MetaModelCodec.encode(cm)）}
```

- 目录序 == blob 序（按类名排序写）；`MetaModelIndex.read` 顺序读全部，`find(className)` 查目录后按 offset 定位单 blob 解析；
- 类名为 JVM 内部名（`org/example/User`）；
- **每 blob 自包含（每类一池），不设跨类共享池**（与 JAR 装 `.class` 同模型）：`find()` 只解析单个 blob、无需加载其他类；与按类 `.beaninfo` 同构可互转。跨类重复的池条目（`java/lang/String`、`id`/`name` 等）约 50–150B/类，换取独立解析与随机访问；若未来对体积极端敏感，可加可选共享字符串段（版本号 +1，破坏自包含，不建议现在做）。

## 6. 加载器

`BeanInfoJson.loadFor(clazz)`：
1. 依次尝试 `<SimpleName>.beaninfo`（v2 二进制）→ `<SimpleName>.beaninfo.json`（v1，兼容期）→ 无则 `None`（回退运行时 `BeanInfos.get`）；
2. v2 解析：`MetaModelCodec.parse` → **`MetaModel.ClassMeta`**（纯数据：属性/构造器/方法记录 + 池字符串；只解析类与类型 `classFor`，**不做任何方法解析**——native-image 等场景可只 parse 不构造）；编码侧 `BeanMetaConverter.from(bi)` 把 BeanInfo（编译期 dig）转成 `ClassMeta` 再 `MetaModelCodec.encode`；
3. 从 `MetaModel.ClassMeta` 构造 `BeanInfo` 的方法**暂不提供**：留待未来经 `BeanInfo.Builder` 反向构造（addField/addCtor + `build()` 按**命名约定**从 `getMethods` 发现 getter/setter——`getPropertyName` 处理 Java bean `getX`/`setX` 与 Scala `x`/`x_$eq`；同名同参优先非 bridge、setter 参数用 `typeinfo.clazz`，见 §5.1 注）；构造完成后 `BeanInfos.cache.update`；
4. 并发：缓存更新需同步（`BeanInfos.cache.update` 幂等，可 double-checked）。

## 7. 生成器

`BeanInfoJsonGenerator` 升级：
- 默认输出二进制 `<SimpleName>.beaninfo`（仍要求先 `BeanInfos.cache.of(...)` 走编译期 dig，保证 `int` 精度）；
- 增加 `--json` 调试导出（人读）、`--dump`（打印解析结果）选项；
- 旧 `.beaninfo.json` 不再生成（加载兼容保留）。

**commons 侧调试导出**（只出不入）：`MetaModelJson.toJson(parsed)` 将 `MetaModel.ClassMeta` 渲染为人读 JSON（类型用 `TypeInfo.name`、ctor 默认值转字符串、None→null）；CLI 直接打印文件内容：

```
java -cp <classpath> org.beangle.commons.bean.meta.MetaModelJson <file.beaninfo>...
```

## 8. 示例

### 8.1 Name（两个 String 属性，默认构造器被跳过）

```
池:
 0: org/beangle/data/hibernate/model/Name
 1: first        2: last
 3: java/lang/String
 4: first_$eq    5: last_$eq

properties count=2:
  p1: name=1, typeinfo(general clazz=3 args=0), getter=1,
      setter=4, flags=0
  p2: name=2, typeinfo(general clazz=3 args=0), getter=2,
      setter=5, flags=0
ctors count=0, methods count=0
```

### 8.2 User.times（Map[Int, WeekTime] —— 精度关键案例）

```
typeinfo: clazzIdx=scala/collection/mutable/Map argCount=2（压平，无 kind 标记；读取时 isIterableType(clazz) → IterableType）
  arg0: clazzIdx=int        ← 编译期 dig 才有，运行时反射会是 java/lang/Object
  arg1: clazzIdx=org/beangle/commons/lang/time/WeekTime
```

### 8.3 User.role（Option[Role] —— 属性级 option）

```
属性记录 typeinfo: clazzIdx=org/beangle/data/hibernate/model/Role argCount=0（元素类型）
属性记录 flags:     bit1=isOptional → 构造时 OptionType(Role)
（ctor 参数/嵌套泛型里的 Option 则存 clazzIdx=scala/Option + args=[Role]）
```

### 8.4 字节级示例（User 类，3 个属性，逐字节对照）

> 示例类为简化版（3 个代表性属性：primitive / 引用 / option 包装）；真实 data 工程的
> User 有更多字段，编码规则完全相同。以下字节流来自真实编码（140B），每行 16 字节，
> 左侧 4 位十六进制为该行首字节在文件内的偏移。

```scala
class User(var id: Long, var name: String, var nick: Option[String])
// 3 属性：id: Long、name: String、nick: Option[String]
// var → scalac 生成 getter id()/name()/nick() 与 setter id_$eq/name_$eq/nick_$eq
// primary ctor 3 参数（均无默认值）；无其它 public 方法 → methods 段 count=0
```

```
偏移   字节（hex）                                                      ASCII
0000  42 4E 49 32 00 02 00 00 00 80 00 04 00 00 25 6F  |BNI2..........%o|
0010  72 67 2F 62 65 61 6E 67 6C 65 2F 64 61 74 61 2F  |rg/beangle/data/|
0020  68 69 62 65 72 6E 61 74 65 2F 6D 6F 64 65 6C 2F  |hibernate/model/|
0030  55 73 65 72 00 00 02 69 64 00 00 04 6E 61 6D 65  |User...id...name|
0040  00 00 04 6E 69 63 6B 01 00 00 00 10 03 00 81 80  |...nick.........|
0050  01 00 00 82 80 08 00 00 83 80 08 02 02 00 00 00  |................|
0060  13 01 03 00 81 80 01 00 00 82 80 08 00 00 83 81  |................|
0070  09 80 08 00 03 00 00 00 02 00 00                 |...........|
```

#### 8.4.1 头部（偏移 0x00–0x09，10B，见 §2）

```
42 4E 49 32 | 00 02 | 00 00 | 00 80
```

| 字节 | 值 | 含义 |
|---|---|---|
| `42 4E 49 32` | `"BNI2"` | magic（0x42 0x4E 0x49 0x32） |
| `00 02` | version = 2 | 大端序 u16 |
| `00 00` | flags = 0 | 保留 |
| `00 80` | nameIdx = 128 | 本类名 = 显式池首项（内置区 0–127 之后，§3）（自描述/校验） |

#### 8.4.2 常量池（偏移 0x0A 起，见 §3）

```
00 04                     poolSize = 4（显式条目数，u16；内置区 0–127 不进池，§3）
```

| 池索引 | 偏移 | 条目字节 | 字符串 |
|---|---|---|---|
| 128 | 0x0C | `00` `00 25` + 37B | `org/beangle/data/hibernate/model/User` |
| 129 | 0x34 | `00` `00 02` + 2B | `id` |
| 130 | 0x39 | `00` `00 04` + 4B | `name` |
| 131 | 0x40 | `00` `00 04` + 4B | `nick` |

- 每条目 = `tag u8(=0)` + `len u16` + UTF-8 字节。如池[128]：`00 | 00 25 | 6F 72 67 2F ...`（类名，37B）。
- **内置类型不进池**：`long` = 固定索引 1、`java/lang/String` = 8、`scala/Option` = 9（§3 表）——类型名引用直接命中，本 blob 省 41B 池开销；
- **无任何访问器名**：`id_$eq`/`name_$eq`/`nick_$eq` 都不在池中——二进制只存声明（属性名+类型），方法名由构造期命名约定发现（§5.1）；
- `scala/Option`（内置索引 9）由 **ctor 参数 nick** 引用（压平后 Option 的 clazz 显式索引）；**属性 nick 不引用它**——属性级 Option 存元素类型 + flags bit1（§4/§5.1）。
- 池尾 = 0x0C + Σ(3+len)，即 0x47，下一段从 0x47 开始。

#### 8.4.3 properties 段（偏移 0x47，tag=1，段长 16B，见 §5.1）

```
01 | 00 00 00 10 | <16B payload>
tag    length u32   后随 payload
```

段框架 = `tag u8 + length u32 + payload`（§5）：`01` 为 properties 段 tag；`00 00 00 13` 是 **u32 大端长度** = 0x10 = 16，即后随 payload 的字节数（count `03` 占 1B + 三条记录 5+5+5B）。解码器先读 5 字节框架，再按 length 读 payload；未知 tag 则整体跳过这 length 字节。对应 hex dump：`01` 在偏移 0x47，`00 00 00 13` 在 0x48–0x4B，payload 从 0x4C 起。

payload 开头 `03` = 3 个属性（u8，§5.1），其后是 3 条记录（每条 = nameIdx + TypeInfo（压平 clazz+args）+ flags；**纯声明，无任何访问器字段**，见 §5.1 注）。

**记录 1：`id`（5B）**

```
00 81 | 80 01 | 00
name    typeinfo   flags
```

| 字节组 | 值 | 含义（§引用） |
|---|---|---|
| `00 81` | nameIdx = 129 | 属性名 `id`（显式池[129]） |
| `80` | Form 1 标记字节：bit7=1 + argCount=0（低 7 位） | 内置 typeinfo 快速形态（§4） |
| `01` | clazzIdx = 1 | `long`（**内置索引**，§3；primitive，JVM 名） |
| `00` | flags bit0 = 0 | 非 transient；bit1=0 非 optional |

（无 getter/setter 名字段——构造时按命名约定发现 `id()`/`id_$eq(long)`，setter 参数 = typeinfo.clazz = `long`。）

**记录 2：`name`（5B）**

```
00 82 | 80 08 | 00
```

- nameIdx = 130（`name`）；TypeInfo：clazzIdx=8（**内置** `java/lang/String`）、argCount=0（u8）；flags=0。

**记录 3：`nick`（5B，option 属性示例）**

```
00 83 | 80 08 | 02
```

- nameIdx = 131（`nick`）；
- TypeInfo：**存元素类型** `String`（clazzIdx=8、argCount=0）——Option 包装不占字节（§4）；
- flags = `02`：bit0=0 非 transient；**bit1=1 isOptional** → 构造时 `OptionType(String)`，元素精度 `String` 由编译期 dig 保证（§引言）。

#### 8.4.4 ctors 段（偏移 0x5C，tag=2，段长 19B，见 §5.2）

```
02 | 00 00 00 13 | <19B payload>
tag    length u32   后随 payload
```

payload：`01`（1 个构造器，u8）→ `03`（3 参数，u8）→ 每参数 `nameIdx + TypeInfo + defaultTag`：

```
00 81 | 80 01 | 00       参数 id:   name=129, clazz=long args=0, defaultTag=0（无默认值）
00 82 | 80 08 | 00       参数 name: name=130, clazz=String args=0, defaultTag=0
00 83 | 81 09 80 08 | 00  参数 nick: name=131, clazz=scala/Option（内置 9）args=[String], defaultTag=0
```

参数 nick 的 Option **按压平 clazz+args 存储**（`scala/Option` 内置索引 9 + 元素 `String` 内置索引 8；argCount=1 为 u8）——ctor 参数无属性级 isOptional 标志可用（§4）；读取时 `TypeInfo.get(scala.Option, [String])` 还原为 `OptionType`。

有默认值时 defaultTag 携带类型与值（tag 1-11，见 §5.2），如 `name: String = "default"` 会编为 `00 0A <池索引>`（tag=10 string，池索引指向 `"default"` 字符串）。

#### 8.4.5 methods 段（偏移 0x74，tag=3，段长 2B，见 §5.3）

```
03 | 00 00 00 02 | 00 00
tag    length u32   count=0
```

`00 00` = count 0（**u16**，§5.3 注）：本类除 accessor 外无其它 public 方法（`getClass`/`toString` 等被忽略，§5.3 注释）。若类含 `getDisplayName()` 等，此段逐条记录 `nameIdx + paramCount + paramTypeIdx...`（无声明类字段，同 properties；`parse` 以原始字符串保留在 `MetaModel.Method`，构造 BeanInfo 时（未来）才解析成 Method）。

#### 8.4.6 大小对照

123B = 10（头部）+ 2（池大小）+ 59（显式池条目）+ 21（properties 段）+ 24（ctors 段）+ 7（methods 段）；
单条属性记录 5B、单条 ctor 参数 5–7B（计数为 u8，内置 typeinfo 用 Form 1 再省 7B）；内置类型索引省去 41B 池开销（§3）。同规模 v1 JSON 需为键名/引号/逗号付出约 5–7 倍体积（§10：15 属性类 JSON 7.4KB vs 二进制 ≈0.8–1.1KB）。

## 9. 与 v1 JSON 字段映射（信息无丢失）

| v1 JSON | v2 |
|---|---|
| `clazz` | 头部 nameIdx |
| properties[].name | nameIdx |
| typeinfo.kind（option/iterable/general） | 压平 clazz+args，无 tag；kind 读取时经 `TypeInfo.get(clazz, args)` 还原 |
| typeinfo.clazz / args / elementType | clazzIdx / 递归 TypeInfo；属性级 Option 存元素类型 + flags bit1（isOptional） |
| getter/setter 的 declaring+name+params | 不编码（纯声明：属性名+类型+flags；访问器由构造期命名约定发现） |
| transient | flags bit0 |
| （v1 无） | ctors 段（参数名+类型+defaultValue） |
| methods {declaring,name,params} | methods 段（同构，无声明类） |

## 10. 尺寸估算（User：JSON 7.4KB）

- 池：~20 个显式字符串 × 平均 ~25B ≈ 500B（类名/属性名/方法名各存一次；内置类型走固定索引、不进池，再省 ~40B/类）
- 属性表：15 × (2 + typeinfo~5–10 + 1) ≈ 120–195B（nameIdx + typeinfo + flags；纯声明，无访问器字段）
- 合计 ≈ **0.8–1.1KB**（较 JSON 省 ~85%）

### 10.1 实测对比（MetaModelJson vs MetaModelCodec，同一 ClassMeta）

| 类 | 属性数 | 二进制 | JSON（紧凑） | 倍数 |
|---|---|---|---|---|
| CodecSample | 7 | 233B | 755B | 3.2× |
| CodecValue | 8 | 225B | 1,399B | 6.2× |
| CodecCollections | 4 | 173B | 743B | 4.3× |
| CodecCtor | 3 | 139B | 455B | 3.3× |
| CodecJson | 5 | 181B | 1,000B | 5.5× |
| **合计** | | **951B** | **4,352B** | **4.6×** |

- 上表为同一批 `ClassMeta` 的 `MetaModelCodec.encode`（二进制）与 `MetaModelJson.toJson`（UTF-8 字节）实测；
- JSON 为**紧凑格式**（无缩进/无多余空白）；v1 JSON（含 getter/setter/methods 全量签名元数据）同规模约 **5–7×**（§10 估算的 7.4KB/15 属性类按此口径）；
- 二进制体积受属性数/ctor 参数数影响，内置类型索引进一步压低池开销（§3）。

## 11. 前向兼容

- 未知段 tag → 按 length 跳过（小幅演进无需升版本）；
- 结构性变更 → version 递增；
- 加载器同时支持 v1 JSON（兼容期）与 v2。

## 12. native-image

- 资源模式：`.*\\.beaninfo`（旧 `.*\\.beaninfo\\.json` 兼容期保留）；
- 解析为纯字节遍历（`parse` 阶段无任何方法解析；未来构造 BeanInfo 时的 `getMethods` 解析已由 reflect-config 的 allDeclaredMethods 覆盖）；
- blob 自包含 → 与 `beaninfo.idx` 目录索引（name → offset/length）天然兼容。

## 13. 决策记录

1. ctors 的 defaultValue：**全量类型标记编码**（§5.2，tag 0-11，已实现并通过测试）；
2. 扩展名：**`.beaninfo`**（magic `BNI2` 已标识格式，无需 `.bin` 后缀）；
3. methods 段：**纳入全部 `bi.methods`**（内容来自 `Builder.build()`/`BeanInfoLoader` 的反射收集，擦除签名、无编译期精度；`parse` 以原始字符串保留在 `MetaModel.Method`，构造 BeanInfo 时（未来）才经 `getMethods` 解析成 Method）；
4. 生成器是否默认同时输出 `--json` 调试文件：**待 data 集成时决定**（仅开发期选项）；
5. 属性/方法**不编码声明类**，**setter 参数类型也不编码**（恒等于 `typeinfo.clazz`）：构造时在本类经 `getMethods` 按 name+参数类型匹配重建（继承解析、优先非 bridge，与 dig 的 Builder 同构）；每属性省 2×u16（声明类）+ 2B（setterParam）、methods 每条省 2B（v2 仍在草案期、未发布，属结构性调整，无兼容负担）；
6. **不提供 `toBeanInfo`**：`parse` 产出 `MetaModel.ClassMeta` 后，构造 `BeanInfo` 留待未来经 `BeanInfo.Builder` 反向接线（addField/addCtor + `build()` 自行发现 accessor），属后续工作；
7. **getterName/setterName 不编码（纯声明化，推翻早期"保留"决定）**：二进制属性记录只存 `nameIdx + typeinfo + flags`；访问器由构造期 `BeanInfo.Builder` 的命名约定发现（`getPropertyName` 处理 Java bean `getX`/`setX` 与 Scala `x`/`x_$eq`），Java 兼容由构造路径保证——方法名属实现视角，不落盘；
8. **TypeInfo 压平**：二进制无 option/iterable/general 标记，一律 `clazzIdx + argCount + args`；kind 读取时经 `TypeInfo.get(clazz, args)` 还原（Option/Iterable 由 clazz 判定）。属性级 Option 存**元素类型** + `MetaModel.Property.isOptional`（flags bit1，构造时 `OptionType(typeinfo)`），`scala/Option` 不入池；构造器参数/嵌套 Option 仍按 `scala/Option + [X]` 存储；
9. **getter/setter 直接为 `Option[MethodHandle]`**：构造期一次 unreflect（`Builder.unreflect`，非 public 类 setAccessible 兜底），`Properties`/`Jexl3` 直接 `handle.invoke`，无懒加载/双重检查；方法名不存储（决策 7），encode 不需要恢复方法名，`reflectAs` 不再使用（其桥接恢复缺陷也随之无关）；类型/参数信息已在二进制记录、方法注解暂不设计。
10. **内置类型区（保留索引 0–127，0 字节）**：primitive/`java/lang/String`/`scala/Option`/装箱类型（0–17）+ 常用日期/时间/日历类型（18–38，java.util/java.sql/java.time 及 Calendar/TimeZone/Locale）共 85 个名字不进池，类型名引用直接命中内置索引（编码 `typeIndex` 查表、解码 `idx<85→内置表`）；显式池条目从索引 128 起（85–127 保留，加内置名不动边界）。每 blob 省 ~40–80B 池开销（§8.4：181B→140B）；内置表增删改变索引空间 → 结构性变更走版本号。

## 14. 与 protobuf / MessagePack 对比（设计回顾）

| 维度 | v2 | protobuf | MessagePack |
|---|---|---|---|
| schema | 无外部 schema（格式固定，按版本演进） | 必需 .proto（字段号+wire type） | 无 schema（自描述） |
| 自描述 | 部分（池+段自包含，结构靠位置） | 否 | 每值带类型 tag |
| 逐值开销 | 0（positional，解析器按位置读） | ~1–2B/字段（tag） | 1B/值（类型 tag） |
| 字符串去重 | 常量池+内置前缀 | 无 | 无 |
| 随机访问 | blob 自包含+idx 目录 | 需整消息 | 需整对象 |
| 泛型精度 | 编译期 dig（不擦除） | schema 可声明，运行时数据无 | 运行时数据 |

- **紧凑来源**：positional（零逐值 tag）+ 常量池去重 + 内置类型前缀；同规模实测 154–260B/类（§10.1），protobuf/msgpack 对重复类型名场景会明显更大；
- **已借鉴**：length-delimited 段 + 未知段按长度跳过（≈protobuf unknown-field）；append-only 演进（内置区保留 0–127 + 段 tag + 版本号 ≈ protobuf"加字段号永不复用"）；
- **候选（暂不采纳，需要时版本号+1）**：① varint 编码小索引/计数/段长（收益 ~3–5%，代价字节规范复杂化）；② record 级字段 tag（新增 record 字段不破坏旧读，代价每记录 +1–2B）；
- **不借鉴**：msgpack 逐值类型 tag（positional 已零开销，人读有 MetaModelJson）；protobuf schema 文件（格式固定、无代码生成需求）。

### 14.1 字符与整数的编码技巧对比

**整数**：
- protobuf：**varint**（base-128，7bit/字节+继续位，低 7 位组在前；`<128` 1B、64 位最多 10B）；负数补码扩展极费（`int64` 恒 10B），`sint` 用 **ZigZag**（`-1→1、1→2`）让小负数 1B；`fixed32/64` 定宽兜底。赌"小值居多"；
- msgpack：**fixint**（`0x00–0x7F`/`0xE0–0xFF` 值即 tag，1B）+ 类型 tag（`0xCC–0xCF` 定宽 uint、`0xD0–0xD3` 定宽 int）+ 大端。自描述优先，tag 必付、值定宽可预测；
- 例：`300` → protobuf `AC 02`（2B）、msgpack `CD 01 2C`（3B）；`1000` → protobuf `E8 07`（2B）、msgpack `CD 03 E8`（3B）。

**字符串**：
- protobuf：length-delimited（wire type 2）= 字段 tag + varint 长度 + UTF-8；**无字符串类型标记**（是否字符串由 schema 决定）；
- msgpack：fixstr/str8/16/32（长度类内联在 tag，`"abc"`→`A3 61 62 63`）；
- 两者**每次出现都付开销、内容不去重**。

**对照 v2**：字符串靠**常量池去重 + 内置前缀**（类型名 0 字节）——同一字符串 msgpack/protobuf 重复付，我们付一次 + 2B 索引引用；整数用定宽 u16/u32（无 varint）。
**可借鉴**：varint 可套用到 `count`/`argCount`/`poolSize`/池条目长度/段长（皆小值，每处省 1B，总量约 4–8%），代价是字节规范复杂化（§8.4 示例不复直观），暂不采纳；ZigZag 无意义（不存裸有符号整数）；msgpack fixint 思路对 ctor 默认值小整数可用但收益极小。
11. **计数字段用 u8（上限 255，编码时校验）**：TypeInfo `argCount`、properties count、ctors count、ctor `paramCount` 均为 u8（实际值域 0–3 / <100 / <5 / <30，绰绰有余）；method `paramCount` 本就 u8（JVM 方法参数上限恰为 255）；**methods count 保持 u16**（大型类非属性方法数可能超 255，留余量）；`poolSize` 与字符串长度保持 u16（可合法超 255）。§8.4 示例 140B→130B（~7%）；定宽 u8 比 varint 简单且常见范围收益相同。
12. **TypeInfo 内置快速形态（Form 1）**：内置 clazz（索引 < 128）且 argCount < 128 时，typeinfo 用 `[1B: bit7=1 | argCount(7bit)] [1B clazzIdx]`（2B，省 1B）——内置类型占 typeinfo 绝大多数，整体再省 ~5%；显式/argCount≥128 走 Form 2 `[2B clazzIdx u16][1B argCount]`（3B 不变）。**该标记只有在大端下成立**：Form 2 首字节即 u16 高字节，显式索引 < 0x8000（编码校验，池索引上限收紧为 0x8000）时高字节 bit7 恒 0，零冲突（小端下会撞）。§8.4 示例 130B→123B。
