# Beangle AOT (Ahead-of-Time) Usage Guide

> Generating GraalVM native-image configuration files with the beangle AOT system.

## 1. Architecture

```
org.beangle.commons.aot
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

## 3. SBT Integration (AotPlugin)

Enable `AotPlugin` in your project to auto-generate configs at build time:

```scala
// build.sbt
lazy val myModule = (project in file(".")).enablePlugins(AotPlugin)
```

The plugin:
1. Reads `src/main/resources/META-INF/beangle/aot-registrars.txt` as the contract:
   one `AotHintRegistrar` class name per line (`#` comments and blank lines allowed)
2. Calls `registering()` on each listed registrar, collects `AotHints`
3. Fails the build if any declared class is missing/invalid, or if the run does not
   stabilize after retries — a clean build must always embed the full config
4. Writes GraalVM config files to `Compile / resourceManaged`; files are included
   in the packaged JAR automatically

If `aot-registrars.txt` is absent or empty, generation is skipped (the project has
no AOT hints) and stale configs from previous runs are removed.

### How It Works

`AotPlugin` launches `AotHintGenerator` as a subprocess against the compiled classes:

```bash
java -cp <classpath> org.beangle.commons.aot.AotHintGenerator \
  -o target/resource_managed/main/META-INF/native-image \
  target/classes
```

## 4. CLI Usage (AotHintGenerator)

Run directly without SBT:

```bash
# Generate configs from compiled classes
AotHintGenerator target/classes

# Custom output directory
AotHintGenerator -o src/main/resources/META-INF/native-image target/classes

# Multiple classpath entries
AotHintGenerator target/classes lib/dependency.jar
```

### Generated Files

| File | Purpose | When Generated |
|------|---------|----------------|
| `reflect-config.json` | Classes needing reflection access | `types` non-empty |
| `resource-config.json` | Resource patterns to include | `patterns` non-empty |
| `proxy-config.json` | JDK dynamic proxy interfaces | `proxies` non-empty |
| `serialization-config.json` | Java serialization support | `serializables` non-empty |

Stale files from previous runs are automatically deleted when the corresponding
hint category becomes empty.

## 5. AotHints API

```scala
val hints = new AotHints

// Register
hints.registerType(classOf[User])
hints.registerPattern("META-INF/custom.idx")
hints.registerProxy(classOf[UserService])
hints.registerSerializable(classOf[UserDto])

// Read
hints.getTypes        // Set[Class[_]]
hints.getPatterns     // Set[String]
hints.getProxies      // Set[List[Class[_]]]
hints.getSerializables // Set[Class[_]]

// Merge
hints.addAll(otherHints)

// Check
hints.isEmpty         // Boolean
```

## 6. Full Example

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

// 2. SBT: enable AotPlugin
// build.sbt
lazy val app = (project in file(".")).enablePlugins(AotPlugin)

// 3. Build
// sbt compile
// → target/resource_managed/main/META-INF/native-image/
//   ├── reflect-config.json
//   ├── resource-config.json
//   ├── proxy-config.json
//   └── serialization-config.json

// 4. Native build
// native-image -jar app.jar
```
