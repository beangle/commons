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
    AotHints  (types, patterns, proxies, serializables)
        │
        ▼
AotHintGenerator.write(outDir, hints)
        │
        ▼
  META-INF/native-image/
  ├── reflect-config.json
  ├── resource-config.json
  ├── proxy-config.json
  └── serialization-config.json
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
registered as reflection types:

```scala
import org.beangle.commons.bean.meta.MetaRegistrar

class AppRegistry extends MetaRegistrar {
  register(classOf[User], classOf[Role])
  // Optional: additional AOT hints
  hints.registerPattern("META-INF/custom.idx")
  hints.registerProxy(classOf[UserService])
}
```

## 3. Registration Strategy (AotPolicy)

`registerType(clazz)` 默认按「安全 + 高性能」策略展开，无需任何配置：
不注册字段、只开放 public 方法 + public 构造器（可反射调用）、不递归父类。
`Public` 可见性依赖 GraalVM `allPublic*` 语义，天然覆盖继承链上的 public 成员，
因此不需要递归也能完整支持对继承方法的反射调用。

两个正交维度（编码在 `AotPolicy.Category` 的命名中）：

| 维度 | 取值 | 说明 |
|------|------|------|
| 可见性 | `Public*` / `Declared*` | `Public*` 含继承链（GraalVM `allPublic*`）；`Declared*` 仅本类声明，继承成员需 `recursive = true` |
| 访问深度 | 无前缀 / `Query*` | 无前缀（如 `PublicMethods`）可反射调用（`method.invoke`/`field.get`/`set`），对应 `all*` 标志；`Query*`（如 `QueryPublicMethods`）仅登记元数据（`getMethod`/`getAnnotation`，调用会失败），对应 `queryAll*` 标志，镜像更小 |

> **invoke vs introspect（Query）**：`getMethod`/`getDeclaredMethods`/`getAnnotation` 这类
> 「查元数据」只需 introspect 级登记；`Method.invoke`、`Field.get/set` 需要 invoke 级登记。
> `queryAll*` 只登记元数据，不登记方法体/invoker stub，镜像更小、构建更快，但运行时
> 反射调用会抛异常。Spring AOT 的 `MemberCategory` 即按此映射（`INTROSPECT_*` → `queryAll*`，
> `INVOKE_*` → `all*`）。字段在本版本 GraalVM（21.x）没有 query-only 批量标志：
> 注册了字段即可读写。

默认策略可通过两种方式定制（均比默认调用繁琐）：

```scala
// 方式一：整个 registrar 换默认策略（覆写 aotPolicy）
class WideHints extends AotHintRegistrar {
  override protected def aotPolicy: AotPolicy =
    AotPolicy(Set(AotPolicy.Category.DeclaredMethods,
                  AotPolicy.Category.DeclaredConstructors,
                  AotPolicy.Category.DeclaredFields), recursive = true)
  override def registering(): Unit = hints.registerType(classOf[User])
}

// 方式二：对个别类显式指定策略
class MixedHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerType(classOf[User]) // 默认：public 方法+构造器、无字段、不递归
    hints.registerType(classOf[Role],
      AotPolicy(Set(AotPolicy.Category.DeclaredMethods, AotPolicy.Category.DeclaredFields),
                recursive = true))
  }
}
```

同一个类被多次注册时策略取并集（类别合并、`recursive`/`unsafeAllocated` 取或）。

### 枚举类型自动注册（含伴生对象）

枚举的运行期反射路径（`EnumConverters`/`Enums`/`Reflections.getInstance`）需要读取字段：
Scala 3 enum 经伴生对象 `MODULE$` 取单例、Java enum 经 `$VALUES` 取常量。这些字段均为
`public static`，因此 `AotHints.registerType` 对 enum 类型自动合并 `PublicFields`
（`allPublicFields`）；对 Scala 3 enum 还会**自动增量注册伴生对象**——应用只需注册枚举
类型本身，无需再写 `classOf[Color.type]`。判定规则：

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
    // 只注册 Color；Color$（伴生，MODULE$ 单例入口）由 AotHints 自动增量注册，
    // 两者都自动带上 allPublicFields
    hints.registerType(classOf[Color])
  }
}
```

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

| File | Purpose | When Generated |
|------|---------|----------------|
| `reflect-config.json` | Classes needing reflection access | `types` non-empty |
| `resource-config.json` | Resource patterns to include | `patterns` non-empty |
| `proxy-config.json` | JDK dynamic proxy interfaces | `proxies` non-empty |
| `serialization-config.json` | Java serialization support | `serializables` non-empty |

Stale files from previous runs are automatically deleted when the corresponding
hint category becomes empty.

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

// Read
hints.policy             // AotPolicy (container default)
hints.getTypes           // Set[Class[_]]
hints.getTypePolicies    // Map[Class[_], AotPolicy]
hints.getPatterns        // Set[String]
hints.getProxies         // Set[List[Class[_]]]
hints.getSerializables   // Set[Class[_]]

// Merge
hints.addAll(otherHints)

// Check
hints.isEmpty            // Boolean
```

## 7. Full Example

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
//   ├── reflect-config.json
//   ├── resource-config.json
//   ├── proxy-config.json
//   └── serialization-config.json

// 4. Native build
// native-image -jar app.jar
```
