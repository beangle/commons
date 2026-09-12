# Beangle BeanMeta 提取规则（MetaLoader / MetaLoaderLite / MetaDigger）

> 三个属性提取器的使用场景、识别规则与对齐约定。
> 数据模型见 [metamodel-usage.md](metamodel-usage.md)，二进制格式见 [metamodel-spec.md](metamodel-spec.md)。

## 1. 使用场景

`BeanMeta`（属性 / 构造器 / 类型）有三个来源，按运行环境分工：

| 提取器 | 执行时机 | 运行环境 | 典型用途 |
|---|---|---|---|
| `MetaDigger` | 编译期（inline 宏） | 构建期 | **native 首选**：`MetaRegistrar.register` 把属性固化进 `beanmeta.idx`，运行期零反射；也用于需要泛型精确类型的场景 |
| `MetaLoaderLite` | 运行期反射 | **GraalVM native** | `beanmeta.idx` 未命中时的回退；只读 public 成员，对应最小注册策略 `AotPolicy.default` |
| `MetaLoader` | 运行期反射 | JVM | 没有 `beanmeta.idx` 时的回退；需要 declared 字段/方法，对应 `AotPolicy.bean` |

运行时的选择顺序（`MetaModels`）：

```
MetaModels.get(clazz)          // 1. beanmeta.idx（dig 产物，native/JVM 都优先）
  └─ 未命中 → MetaModels.reflect(clazz)
       ├─ JVM    → MetaLoader.load       // strict
       └─ native → MetaLoaderLite.load   // lite
```

即 `MetaModels.reflect` 只按 `JVM.isGraal` 切换：native 下的运行期反射一律走 lite，JVM 下一律走 strict。
native 下若 `beanmeta.idx` 已覆盖该类，连 lite 都不会走到。

lite 是 native 回退路径的主力，原因在注册成本：它只读 public 方法/构造器，GraalVM 只需登记
`allPublicMethods` + `allPublicConstructors`（`AotPolicy.default`），不递归父类、不读字段；
strict 需要 declared 字段与方法并递归父类/接口（`AotPolicy.bean`），配置规模和镜像体积都更大。

## 2. 属性识别通用规则

只有「public、非 static、非忽略名单」的方法参与识别。属性必须可读：**只写（write-only）
的 setter 不产生属性**，只能回填已有 getter 的写访问器。

### 2.1 getter

0 参数、返回非 `Unit` 的方法，满足以下任一条件即成为属性：

1. **JavaBean 命名**：`getXxx` / `isXxx`（第 4/3 个字符大写）→ 属性名 `xxx`（`lower()`：第二
   字符大写时保持原样，避免 `getURL` → `uRL`）；
2. **同名字段**：方法名与已发现的字段同名（Scala `val`/`var` 的访问器）；
3. **`@property` 显式声明**：`value` 为空取方法名，否则取 `value`；只作用于 0 参非 `Unit`
   方法，带参/`Unit` 方法上的注解一律忽略。

`MetaLoaderLite` 在**工程内声明**的方法上更宽松：public 参数less 非 `Unit` 方法即使不满足
1–3 也登记为属性（属性名保留方法名，如 `iterator`），因此 lite 会多出 strict 看不到的工程内
参数less 属性。

### 2.2 setter

仅识别 `setXxx`（第 4 字符大写）、`x_$eq`（Scala 方法的 JVM 名）、`x_=` 三种形态。
属性名与 getter 对齐后回填 `setterName`；`checkTransient` 会把「无 setter 且不在主构造参数
中」的属性标记为 transient，所以 setter 丢失会连带影响 transient 标记。

### 2.3 忽略名单

- 通用：`hashCode`、`toString`、`wait`、`clone`、`equals`、`getClass`、`notify`、`notifyAll`、
  `apply`、`unapply`、`unApply`、`canEqual`；
- case class 追加：`productArity`、`productIterator`、`productPrefix`、`productElement`、
  `productElementName`、`productElementNames`、`copy`；
- 命名形态：`_` 前缀、名字含 `$`（`x_$eq` 除外）。

## 3. 标准库与继承成员

三条路径对「成员定义在 `scala.*` / `java.*` 基类里」的处理不同。基类的 JavaBean 属性
（如 `java.util.Date.getTime`/`setTime`）是合法 bean 属性，不该因为定义在 JDK 里被丢掉；
但集合 API（`Seq.head`/`size`/`toList`…）不是。

| 成员形态 | strict | lite | dig |
|---|---|---|---|
| `getXxx(): T` | ✔ | ✔ | ✔（`scala.*`）；`java.*` 编译期不可见 |
| `isXxx(): T`（惯例 Boolean，不校验返回类型） | ✔ | ✔ | ✔（`scala.*`）；`java.*` 编译期不可见 |
| `setXxx(v)` | ✔ | ✔ | ✔（`scala.*`）；`java.*` 编译期不可见 |
| 参数less `def x: T`（非 JavaBean 名） | ✘ | ✘ | ✘ |
| `@property` 标注在库基类方法 | ✔ | ✔ | ✘（只认 JavaBean 命名） |

### 3.1 MetaLoader（strict）：不按包排除，只按命名判定

strict 会遍历整个继承链（父类一路到 `AnyRef`，接口全部递归），不做 `scala.`/`java.` 包过滤。
限制属性数量的是 getter 的命名门槛（`isJavaBeanGetter` 或同名字段）。因此：

```scala
class DateBean extends java.util.Date
// strict ⇒ date, day, hours, minutes, month, seconds, time, timezoneOffset, year
//          其中 time/year/month/... 均可写（setTime/setYear/...）
```

代价是 `isXxx` 命名会把 `scala.collection.IterableOnceOps.isEmpty`、
`isTraversableAgain` 这类成员识别成 `empty`、`traversableAgain` 属性——lite/dig 同样如此，
属于已固化的 API 约定。

### 3.2 MetaLoaderLite（lite）：库方法与 bridge 只认 JavaBean getter

lite 只走 `getMethods`，无法区分「工程内参数less 方法」与「继承来的集合 API」（在字节码
层面都是 0 参 public 方法），因此对下面两类方法只保留 JavaBean getter（`getX`/`isX`）或
`@property`：

- `declaringClass` 以 `scala.`/`java.` 开头的方法；
- **bridge 方法**：Scala 3 为继承自 trait 的方法在本类生成 `ACC_BRIDGE|ACC_SYNTHETIC` 的
  forwarder（如 `Seq` 的 `head`/`size`/`isEmpty`），其 `declaringClass` 就是本类，只能按
  `Method.isBridge` 识别。

**setter 不受此限制**：setter 不创建属性，只回填已有 getter 的写访问器；`setXxx` 本身就是
JavaBean 约定，一并限制会让 lite 的读写器（以及 transient 标记）与 strict 不一致。

### 3.3 MetaDigger（dig）：`scala.*` 按命名收录，`java.*` 不参与

- **`scala.*` 基类**：成员树（TASTy/pickle）可读，但只收录 `getXxx`/`isXxx`/`setXxx` 命名的
  成员，且不读这些基类的字段。集合 API 名字不匹配，自然被排除；`isEmpty` 这类 JavaBean
  成员与 strict/lite 一致。
- **`java.*` 基类**：编译期拿不到成员树，直接跳过；且 `MetaLoader.supports` 不允许把 JDK 类
  作为反射入口，也不能走运行期合并。工程内声明的 Java 父类/接口不在此列：dig 会把它们交给
  运行期 `MetaLoader` 合并（`javaBases`）。

因此 `class X extends java.util.Date` 这样的直接继承 JDK bean 类，dig 不会产出继承属性。
需要覆盖时，请插入一个工程内的 Java 父类，或用 `@property` 显式声明。

## 4. 三路差异示例

以下结果均由 `MetaLoader` / `MetaLoaderLite` / `MetaModels.of`（dig）实测得到，属性名已排序。

### 4.1 工程内参数less 方法

```scala
class PageBean(val pageIndex: Int, val items: collection.Seq[String]) {
  def totalPages: Int = 1
  def hasNext: Boolean = false
  def iterator: Iterator[String] = items.iterator
  def size: Int = items.size
}
```

| 路径 | 属性 |
|---|---|
| strict | `items`, `pageIndex` |
| lite | `hasNext`, `items`, `iterator`, `pageIndex`, `size`, `totalPages` |
| dig | `hasNext`, `items`, `iterator`, `pageIndex`, `size`, `totalPages` |

strict 只认「同名字段 + JavaBean 命名 + `@property`」，所以只有两个字段属性；lite/dig 把工程内
声明的参数less 方法一律当属性，连 `size` 也会进来（它是工程内声明，不属于标准库集合 API）。
dig 还要求「没有任何参数段」，因此 `def next(): Page[E]` 这种空括号方法只有 lite 收录。

真实类 `SinglePage[String]` 正好覆盖这个差异：

| 路径 | 属性 |
|---|---|
| strict | `empty`, `hasNext`, `hasPrevious`, `items`, `length`, `pageIndex`, `pageSize`, `totalItems`, `totalPages`, `traversableAgain` |
| lite | strict + `iterator`, `next`, `previous` |
| dig | strict + `iterator` |

`next`/`previous` 是 `def next(): Page[E]`（空括号），dig 不收录；`empty`/`traversableAgain`
来自标准库基类，三路一致（见 3.2/3.3）。

### 4.2 继承 JDK bean 基类

```scala
class OrderBean extends java.util.Date {
  def title: String = "t"
  def getOwner: String = "o"
}
```

| 路径 | 属性 |
|---|---|
| strict | `date`, `day`, `hours`, `minutes`, `month`, `owner`, `seconds`, `time`, `timezoneOffset`, `year` |
| lite | strict + `title` |
| dig | `owner`, `title` |

strict/lite 都能拿到 JDK 基类的 JavaBean 属性，且 `setDate`/`setHours`/`setTime`… 都在（strict
与 lite 的 getter/setter 完全一致）；dig 因为 `java.*` 基类没有成员树，只剩工程内声明的
`owner`/`title`。

> `MetaModels.of(classOf[X])` 是 inline 宏，参数必须是类字面量；用 `Class[_]` 变量传入会在
> 编译期宏展开时报错。

## 5. 已知差异

### 5.1 lite 相对 strict 的信息缺失

lite 只读 public 方法，不读字段、不读默认值，也不做继承链泛型推导，因此有三类信息差：

| 信息 | strict | lite | 影响 |
|---|---|---|---|
| `@transient` 字段标记 | 保留（`token.isTransient = true`） | 丢失（`false`） | 序列化 / 属性复制 / ORM 映射会把本应跳过的属性当普通属性 |
| 构造器默认值 | 保留（`Param(id, Long, Some(1))`） | 无（`None`） | 实例化 / 表单回填拿不到默认值 |
| 类型变量（泛型） | 继承链上可解析（`items: List`） | 退化为 `Object` | 属性类型精度下降 |

```scala
// @transient：strict 标记 transient，lite 不标记（lite 无字段信息）
class TransientBean {
  @transient var token: String = ""
  var name: String = ""
}

// 泛型继承：strict 借继承链推导得到 List，lite 不推导只能是 Object
class BaseBox[T <: collection.Seq[String]] { var items: T = null.asInstanceOf[T] }
class StringBox extends BaseBox[List[String]]

// 构造器默认值：strict/dig 有，lite 无
class DefaultedBean(val id: Long = 1L, val name: String = "n")
```

这一项只出现在「类型变量本身就是属性类型、且擦除后是集合」的属性上：`var items: T`，
`T <: collection.Seq[String]` → strict 得到 `List`，lite 得到 `Object`。标量类型变量
（`var value: T`）两边都退化为 `Object`；元素类型写在 `ParameterizedType` 里的
（`var items: Seq[T]`）两边也都不解析元素类型，同样退化为 `Seq[Any]`，不构成差异。

规避方式：让这些类走 dig（`MetaRegistrar.register`，`beanmeta.idx` 优先于任何反射），或把
属性声明成具体类型 / 用字段承载。

### 5.2 其他

- **dig 不产出 `java.*` 基类属性**（见 3.3），strict/lite 有；
- **lite/dig 比 strict 多认工程内参数less 方法**（如 `Page.iterator`），是有意放宽；
- **空括号方法 `def x(): T`**：字节码与参数less 方法同为 0 参，lite 收录、strict 需同名字段
  或 `@property`、dig 不收录（`paramss` 非空）；
- **库基类上的 `@property`**：strict/lite 生效，dig 只认 JavaBean 命名；
- **属性名集合**：黄金测试覆盖的用例里 lite ⊇ strict（lite 只多不少），setter 与 getterName 优先级两者一致。

## 6. 一致性护栏

`src/test/.../bean/meta/PropertyCompletenessTest.scala` 以黄金清单固定上述规则，防止将来的
优化把 `hashCode`/`getClass`/`copy` 或集合 API 带进 getter，或漏掉已发布属性：

- `junk`：Object 方法、case class 样板；
- `libraryApi`：`size`/`head`/`toList`/`mkString` 等集合 API；
- Java bean、普通 Scala bean、注解属性、Page 实现等用例逐项比对属性名、类型与读写器；
- `MetaLoaderLiteTest`、`MetaDiggerTest`、`PropertyAnnotationTest` 覆盖各自的边界。
