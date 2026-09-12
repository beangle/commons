# Beangle AOT (Ahead-of-Time) Usage Guide

> Generating GraalVM native-image configuration files with the beangle AOT system.

## 1. Architecture

```
org.beangle.commons.aot
├── AotPolicy            # Registration strategy (visibility × access depth)
├── AotHints             # Mutable container for hint data
├── AotHintRegistrar     # Trait for registering hints
└── AotHintGenerator     # CLI tool / writer for GraalVM config files

org.beangle.build.sbt
└── AotPlugin            # SBT plugin for automated config generation
```

### Data Flow

```
AotHintRegistrar.registering()
        │
        ▼
    AotHints  (types, patterns, proxies, serializables, jni)
        │
        ▼
AotHintGenerator.write(outDir, hints)
        │
        ▼
  META-INF/native-image/
  └── reachability-metadata.json    # GraalVM 25 单一合并文件（reflection/resources 同文件）
```

## 2. Creating a Registrar

Implement `AotHintRegistrar` and populate `hints` in `registering()`:

```scala
import org.beangle.commons.aot.AotHintRegistrar

class MyHints extends AotHintRegistrar {
  override def registering(): Unit = {
    // Classes needing reflection access
    hints.registerType(classOf[User], classOf[Role])

    // Resource inclusion patterns (ant-style)
    hints.registerPattern("META-INF/custom.idx")
    hints.registerPattern("templates/**")

    // JDK dynamic proxy interfaces
    hints.registerProxy(classOf[UserService], classOf[RoleService])

    // Java serialization support
    hints.registerSerializable(classOf[UserDto], classOf[RoleDto])
  }
}
```

### With MetaRegistrar (metamodel)

`MetaRegistrar` extends `AotHintRegistrar` — metamodel classes are automatically
registered as reflection types; their property types are also scanned for
Scala 3 enums (recursing into `@component` value types and collection element
types), so enum properties need no manual registration at all:

```scala
import org.beangle.commons.bean.meta.MetaRegistrar

class AppRegistry extends MetaRegistrar {
  register(classOf[User], classOf[Role]) // User 的 enum 属性自动注册（含伴生对象）
  // Optional: additional AOT hints
  hints.registerPattern("META-INF/custom.idx")
  hints.registerProxy(classOf[UserService])
}
```

## 3. Registration Strategy (AotPolicy)

`AotPolicy` 提供三个预定义策略：

```text
AotPolicy.default:                       AotPolicy.bean:
  PublicMethods + PublicConstructors       PublicMethods + PublicConstructors
  无字段                                   DeclaredFields
  不递归                                   DeclaredMethods
                                          recursive = true

AotPolicy.full:
  PublicMethods + DeclaredMethods
  PublicConstructors + DeclaredConstructors
  PublicFields + DeclaredFields
  recursive = true
```

`registerType(clazz)` 使用 `AotPolicy.default`，适用于大多数场景。需要运行时反射
字段/方法元数据时（如 `MetaLoader`），使用 `AotPolicy.bean`：

```scala
hints.registerType(classOf[User], AotPolicy.bean)
```

`AotPolicy.default` 对应 `MetaLoaderLite`（只用 public 成员），`AotPolicy.bean` 对应
`MetaLoader`。两者的属性识别差异见 [metamodel-loader-rules.md](metamodel-loader-rules.md)。

### 为什么需要 AotPolicy.bean

`MetaLoader` 是 beangle 的运行时反射工具（当没有预构建的 `beanmeta.idx` 时的回退路径）。
它需要反射用户的业务类来构建 `BeanMeta`，具体依赖：

| 反射 API | MetaLoader 用途 | 所需 Category |
|----------|----------------|---------------|
| `getDeclaredFields` | 读字段名（用于 `findAccessor` 判断 getter）、读 `@transient` 修饰符 | `DeclaredFields` |
| `getDeclaredMethods` | 读方法名、修饰符、返回类型、泛型签名、注解（`@noreflect`） | `DeclaredMethods` |
| `getGenericSuperclass` / `getGenericInterfaces` | 推导泛型参数类型（`deduceParamTypes`） | 递归 + 元数据查询 |
| `Method.invoke`（`BeanInfo.from` 阶段） | 通过 `MethodHandle` 调用 getter/setter | `PublicMethods`（已覆盖） |

**字段用 `DeclaredFields`、方法用 `DeclaredMethods`**：MetaLoader 只查询方法元数据
（不 invoke），但这不需要专门的类别——GraalVM 25 的 reachability-metadata v1.2.0
已删除 query-only 注册，登记即同时开放查找与调用（见下）。

递归（`recursive = true`）是必须的：`Declared*` 仅覆盖本类声明的成员，
而 `MetaLoader` 需要遍历整个继承链（父类 + 接口）来收集全部字段和方法。

### 类别只表达可见性

| 维度 | 取值 | 说明 |
|------|------|------|
| 可见性 | `Public*` / `Declared*` | `Public*` 含继承链（GraalVM `allPublic*`）；`Declared*` 仅本类声明，继承成员需 `recursive = true` |

`Category` 只保留 `PublicMethods/DeclaredMethods`、`PublicConstructors/DeclaredConstructors`、
`PublicFields/DeclaredFields` 六个值，一一对应 GraalVM 的 `allPublic*` / `allDeclared*` 批量标志。

> **为什么不再有 introspect-only 类别**：`getMethod`/`getDeclaredMethods`/`getAnnotation`
> 这类「查元数据」在 GraalVM 21 时代可以用 `queryAll*` 做仅 introspect 登记（不登记
> invoker stub，镜像更小、构建更快，但运行时 `Method.invoke` 会抛异常；Spring AOT 的
> `MemberCategory` 即按 `INTROSPECT_*` → `queryAll*`、`INVOKE_*` → `all*` 映射）。
> **GraalVM 25 的 reachability-metadata v1.2.0 删除了 `queryAll*`**：反射条目只接受
> `allDeclaredConstructors`/`allPublicMethods` 等 `all*` 标志，且 schema 明确定义为
> "for reflective invocation"（登记即可查找并调用）。若在新格式里继续写 `queryAll*`，
> native-image 会打印 `Warning: Unknown attribute(s) [queryAllXxx] in reflection class
> descriptor object` 并**整条丢弃**，于是 `getDeclaredMethods` 等元数据查询在镜像里失败。
> 因此 beangle 在迁移到新格式时直接把 `Query*` 类别删除，而不是保留成同义别名：
> 需要更小镜像时改用 `methods` 定点注册，而不是退回 `queryAll*`。

可通过预定义策略或自定义策略定制：

```scala
// 方式一：使用预定义的 bean 策略（推荐，适用于运行时反射场景）
class WideHints extends AotHintRegistrar {
  override protected def aotPolicy: AotPolicy = AotPolicy.bean
  override def registering(): Unit = hints.registerType(classOf[User])
}

// 方式二：对个别类显式指定策略
class MixedHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerType(classOf[User])                     // 默认策略
    hints.registerType(classOf[Role], AotPolicy.bean)     // bean 策略（递归 + 字段 + 方法元数据）
  }
}
```

同一个类被多次注册时策略取并集（类别合并、`recursive`/`unsafeAllocated` 取或）。

### 枚举类型自动注册（含伴生对象）

枚举的运行期反射路径不止读字段（Scala 3 enum 经伴生对象 `MODULE$` 取单例、Java enum 经
`$VALUES` 取常量），还会反射方法（`Enums` 的 `valueOf`/`values`/`id`）与属性（带属性的
枚举如 `enum Color(val id: Int)`，`BeanInfos`/`MetaLoader` 会读取字段和 getter）。因此
`registerEnum` 对枚举类、伴生对象与全部值类统一应用 **`AotPolicy.enumPolicy`** 策略
（即 bean + public 字段）
（public 方法/构造器可调用、declared 字段、查询级 declared 方法、递归父类链），
应用只需注册枚举类型本身，无需再写 `classOf[Color.type]`。

经 `MetaRegistrar`（`MappingModule`/`BindModule` 等）注册的类，其属性树会被自动扫描：
遍历属性（含集合/Map 元素类型）、递归 `@component` 值类型，发现 Scala 3 enum 即自动注册
（同样携带伴生对象）。因此 ORM 实体只要 `bind`/`register`，枚举属性**完全无需手工注册**。
仅当枚举不出现在任何已注册类的属性中（如仅作方法参数）时，才需要显式
`hints.registerEnum(classOf[枚举])`（简单路径 `registerType` 不感知枚举特性，
不注册伴生对象与值类）。

判定规则：

- Java enum：`clazz.isEnum`；
- Scala 3 enum：`scala.reflect.Enum` 可赋值；
- Scala 3 枚举伴生（自动增量注册，同样命中规则）：实现 `scala.deriving.Mirror.Sum`。

```scala
enum Color(val id: Int) {
  case Red extends Color(1)
  case Green extends Color(2)
}

class AppHints extends AotHintRegistrar {
  override def registering(): Unit = {
    // 用 registerEnum：Color、Color$（伴生，MODULE$ 单例入口）与全部值类
    // 都按 AotPolicy.enumPolicy（bean + public 字段）注册
    hints.registerEnum(classOf[Color])
  }
}
```

### 代理类与枚举值类的元数据回退

`BeanInfos` 对框架/编译器生成的 `$` 子类会复用父类 BeanMeta 并绑定到子类：

- Hibernate 懒加载代理：`Entity$HibernateProxy`；
- Scala 3 枚举值类：`NoticeStatus$$anon$1`（每个带参 case 的匿名子类）。

回退路径下 `BeanInfo.from` 仍会对子类调用 `getMethods` 绑定 MethodHandle，因此 native 镜像中
需保证父类已登记 public 方法：

- Scala 3 枚举：`registerEnum` 用 `AotPolicy.enumPolicy`（bean + public 字段）注册枚举类、
  伴生对象与全部值类，天然覆盖；
- Hibernate 代理：代理无自有属性，随实体类注册（`AotPolicy.bean`）即可覆盖。

## 4. SBT Integration (AotPlugin)

`AotPlugin` is auto-enabled on every JVM project (no `enablePlugins` needed).
Generation is driven by the anchor files — presence of
`src/main/resources/META-INF/beangle/aot-registrars.txt` and/or a
`src/main/resources/beangle.xml` with declared modules controls whether the
plugin does anything:

```text
src/main/resources/META-INF/beangle/aot-registrars.txt   # one AotHintRegistrar class per line
src/main/resources/beangle.xml                           # <jpa>/<orm><mapping> and <cdi><module> declarations
```

The plugin:
1. Merges the registrar contract from `aot-registrars.txt` (one `AotHintRegistrar`
   class name per line; `#` comments and blank lines allowed) with `beangle.xml`
   declarations (`<jpa>/<orm>` mappings and `<cdi>` modules — `MappingModule`
   subclasses are `MetaRegistrar`/`AotHintRegistrar`)
2. Calls `registering()` on each registrar, collects `AotHints`
3. Fails the build if any declared class is missing/invalid, or if the run does not
   stabilize after retries — a clean build must always embed the full config
4. Writes GraalVM config files to `Compile / resourceManaged`; files are included
   in the packaged JAR automatically

If neither anchor file declares anything, generation is skipped (the project has
no AOT hints) and stale configs from previous runs are removed.

### Registrar 类自注册

native 镜像只收录静态可达的类；仅靠 `aot-registrars.txt`/`beangle.xml` 里的字符串无法把
registrar 类本身带进闭包。因此 `AotHintGenerator` 在处理每个声明类时**自动注册其类自身**，
保证运行期按名实例化（`Reflections.getInstance`/`tryGetInstance`，用于 `Profiles` 加载
`MappingModule`、`EnumConverters` 取枚举单例等）在 native 中可用：

- 普通类（无 `$` 伴生）：注册构造器（`allDeclaredConstructors`），覆盖
  `getDeclaredConstructor().newInstance()`；
- Scala object（存在 `$` 伴生）：伴生类注册构造器 + `allPublicFields`
  （`MODULE$` 为 `public static`，`Reflections` 经 `getField("MODULE$")` 取单例）。

```text
# aot-registrars.txt 声明 SampleMapping（Scala object，经 beangle.xml 同样生效）
org.example.SampleMapping

# 生成物自动包含（无需手写）：
#   {"name":"org.example.SampleMapping",  "allDeclaredConstructors":true}
#   {"name":"org.example.SampleMapping$", "allDeclaredConstructors":true, "allPublicFields":true}
```

## 5. CLI Usage (AotHintGenerator)

Run directly without SBT:

```bash
# Generate configs from a registrars list file
AotHintGenerator --registrars aot-registrars.txt target/classes

# Custom output directory
AotHintGenerator --registrars aot-registrars.txt \
  -o src/main/resources/META-INF/native-image target/classes

# Multiple classpath entries
AotHintGenerator --registrars aot-registrars.txt target/classes lib/dependency.jar

# Help
AotHintGenerator -h
```

The registrars list file declares the `AotHintRegistrar` class names to load,
one per line (`#` comments allowed). Every declared class must be found and
loaded, otherwise the tool exits with a non-zero code.

### Generated Files

只生成**一个** `reachability-metadata.json`（GraalVM 25 schema v1.2.0），所有类别
（reflection / resources / JNI / 序列化 / 代理）都写进这个文件的同名顶层键或条目字段：

| 顶层键 / 条目字段 | 来源 | 何时写入 |
|------------------|------|---------|
| `reflection`（`allPublic*`/`allDeclared*`/`methods`/`fields`） | `types`/`typePolicies`/`constructors` | `types` 或 `constructors` 非空 |
| `reflection[].unsafeAllocated` | `AotPolicy.unsafeAllocated` | 策略置位 |
| `reflection[].jniAccessible` + `methods`/`fields` | `AotPolicy.jniAccessible` / `registerJni*` | 见第 7 节 |
| `reflection[].type.proxy` | `proxies` | `proxies` 非空 |
| `reflection[].serializable` | `serializables` | `serializables` 非空 |
| `resources[].glob` | `patterns` | `patterns` 非空 |

历史遗留的分文件产物（`reflect-config.json`、`resource-config.json`、
`proxy-config.json`、`serialization-config.json`）在重新生成时会被自动删除，
避免 native-image 同时读新旧两种格式造成重复注册。

## 6. AotHints API

```scala
val hints = new AotHints(AotPolicy.default)

// Register (simple: default policy)
hints.registerType(classOf[User])

// Register (custom: explicit policy per class)
hints.registerType(classOf[Role], AotPolicy(Set(AotPolicy.Category.DeclaredMethods),
  recursive = true))

hints.registerPattern("META-INF/custom.idx")
hints.registerProxy(classOf[UserService])
hints.registerSerializable(classOf[UserDto])

// JNI（GraalVM 25，见第 7 节）：JDK 内部类按名字登记，绕过 isJdk 过滤
hints.registerJniMethod("sun.font.Font2D", "charToGlyphRaw", "int")
hints.registerJniField("sun.font.GlyphList", "gposx", "len")
hints.registerJniType("com.example.NativeBridge")

// Read
hints.policy             // AotPolicy (container default)
hints.getTypes           // Set[Class[_]]
hints.getTypePolicies    // Map[Class[_], AotPolicy]
hints.getPatterns        // Set[String]
hints.getProxies         // Set[List[Class[_]]]
hints.getSerializables   // Set[Class[_]]
hints.getConstructors    // Set[String]
hints.getJniTypes        // Set[String]
hints.getJniMethods      // Map[String, Set[(String, List[String])]]
hints.getJniFields       // Map[String, Set[String]]

// Merge
hints.addAll(otherHints)

// Check
hints.isEmpty            // Boolean
```

## 7. JNI 可达注册（GraalVM 25 `jniAccessible`）

JNI 元数据 **只能写在 `reachability-metadata.json`**（旧 `jni-config.json` 仍被兼容读取，
但不再生成）。GraalVM 25 起 JNI 注册折叠为 `reflection` 条目的 `jniAccessible` 字段：

```json
{
  "reflection": [
    { "type": "sun.font.Font2D",
      "jniAccessible": true,
      "methods": [ { "name": "charToGlyphRaw", "parameterTypes": ["int"] } ] }
  ]
}
```

两条必须记住的语义：

1. **只登记类型不足以 `GetMethodID`/`GetFieldID`**。`jniAccessible: true` 只让本机代码能
   `FindClass` 到该类型；通过 `GetMethodID` 查方法时，该方法必须已在同一条目里用
   `methods` 列出（或由 `allDeclared*`/`allPublic*` 批量覆盖）。漏掉方法名的典型报错：

   ```text
   java.lang.NoSuchMethodError: sun.font.Font2D.charToGlyphRaw(I)I
     at ...JNIFunctions$Support.getMethodID(JNIFunctions.java:1948)
   ```

   反向地，只写 `methods` 而不写 `jniAccessible`（且未按 `AotPolicy` 展开）也不会开 JNI。
2. `unsafeAllocated` 与 JNI 的 `AllocObject` 对应，但它是**独立字段**，不需要
   `jniAccessible`。

### 两种登记方式

| 方式 | 写法 | 适用 |
|------|------|------|
| 粗粒度 | `hints.registerType(clazz, AotPolicy(Set(...), jniAccessible = true))` | 能 `classOf` 引用的自有类；成员由 `categories` 展开（`allDeclared*`/`allPublic*` 对 JNI 同样生效） |
| 定点 | `registerJniType` / `registerJniMethod` / `registerJniField` | JDK 内部类、agent 采集的 C→Java 回调；只注册真正被查的名字，镜像最小 |

```scala
class NativeHints extends AotHintRegistrar {
  override def registering(): Unit = {
    // 1) 自有类型：粗粒度，开放全部 public 方法给 JNI
    hints.registerType(classOf[NativeBridge], AotPolicy(Set.empty, jniAccessible = true))

    // 2) JDK 内部类：只能按名字登记（包未导出，classOf 不可用），且必须逐成员列出
    hints.registerJniMethod("sun.font.Font2D", "charToGlyphRaw", "int")
    hints.registerJniMethod("sun.font.Font2D", "charToVariationGlyphRaw", "int", "int")
    hints.registerJniMethod("sun.font.Font2D", "getMapper")
    hints.registerJniField("sun.font.Font2D", "font", "style")
  }
}
```

要点：

- 参数类型按 JSON 写法给（`"int"`、`"char"`、`"java.lang.String"`、
  `"sun.java2d.loops.CompositeType"`）；**构造器用 `"<init>"`**；
- `registerJniMethod`/`registerJniField` **隐式包含该类型的 `jniAccessible`**，
  无需再调 `registerJniType`；
- `registerJni*` 按类名记录、**不走 `isJdk` 前缀过滤**，因此可以登记
  `sun.*`/`com.sun.*` 等 `classOf` 引用不到的内部类；
- `AotPolicy.jniAccessible` 参与 `merge`（取或），同一个类被多次注册时只要有一次置位即生效；
- 精确成员清单建议直接抄 native-image agent 输出，或对 `lib*.so` 做
  `strings` + `javap` 核对（ems 的 `sun.font.Font2D` 回调名即在 JDK 25 由
  `charToGlyph` 更名为 `charToGlyphRaw`，只按旧名登记会在运行期报 `NoSuchMethodError`）。

## 8. Full Example

```scala
// 1. Define hints
class ServiceHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerType(
      classOf[UserService],
      classOf[RoleService],
      classOf[UserServiceBean]
    )
    hints.registerPattern("META-INF/services/**")
    hints.registerProxy(classOf[UserService])
    hints.registerSerializable(classOf[UserDto])
  }
}

// 2. Declare the registrar in an anchor file
// src/main/resources/META-INF/beangle/aot-registrars.txt:
//   org.example.ServiceHints

// 3. Build (AotPlugin auto-enabled)
// sbt compile
// → target/resource_managed/main/META-INF/native-image/
//   └── reachability-metadata.json   # reflection + resources + 代理 + 序列化 + JNI

// 4. Native build
// native-image -jar app.jar
```
