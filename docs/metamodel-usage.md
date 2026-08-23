# Beangle Metamodel Usage Guide

> Usage, architecture, and tooling for the beangle metamodel system.
> For the binary format specification, see [metamodel-spec.md](metamodel-spec.md).

## 1. Architecture

```
org.beangle.commons.bean.meta
├── MetaModel          # Core data model (ClassMeta, Property, Ctor, Method)
├── MetaCodec          # Binary codec for single class blob (BMET magic)
├── MetaIndex          # Multi-class container format (BMXI magic)
├── MetaModels         # Global lookup (full-load-at-startup)
├── MetaLoader         # Runtime reflection → ClassMeta (fallback)
├── MetaJson           # Debug JSON export (one-way)
├── MetaRegistry       # Abstract registry for collecting ClassMeta
├── MetaGenerator      # CLI tool for generating metamodel.idx
├── ClassMetas         # Compile-time entry point (macros)
└── MetaDigger         # Compile-time macro digger
```

### Data Flow

```
Compile time:
  MetaDigger → ClassMeta → MetaCodec.encode → binary blob

Runtime (with binary):
  metamodel.idx → MetaModels → ClassMeta → BeanInfo.from → BeanInfo

Runtime (reflection fallback):
  Class → MetaLoader → ClassMeta → BeanInfo.from → BeanInfo
```

ClassMeta is the single source of truth. Both binary and reflection paths produce ClassMeta;
BeanInfo.from reconstructs BeanInfo with accessor MethodHandles.

## 2. Global Lookup (MetaModels)

`MetaModels` scans `classpath*:META-INF/beangle/metamodel.idx` at startup and loads all ClassMeta into memory:

```scala
import org.beangle.commons.bean.meta.MetaModels

// Lookup by Class — O(1) hash table hit
MetaModels.get(classOf[User]) match {
  case Some(cm) => println(cm.properties)
  case None     => println("not found")
}

// Lookup by class name (dot-separated or JVM internal)
MetaModels.get("org.example.User")

// Check availability
MetaModels.contains(classOf[User])  // Boolean

// List all registered class names
MetaModels.classNames  // Set[String]
```

### Features

- **Full-load-at-startup**: all idx files read sequentially at first access (like ClassLoader.findLoadedClass);
- **String pool deduplication**: shared across all idx files (common strings like "id", "name" stored once);
- **O(1) lookup**: subsequent `get()` calls are hash table hits with no I/O.

## 3. Compile-Time Registration (MetaRegistry)

```scala
import org.beangle.commons.bean.meta.MetaRegistry

class AppRegistry extends MetaRegistry {
  override protected def registering(): Unit = {
    register(classOf[User], classOf[Role])
  }
}

// Collect and encode
val registry = new AppRegistry()
val metas = registry.collect()  // Seq[ClassMeta]

// Write to beaninfo.idx
val out = new FileOutputStream("beaninfo.idx")
registry.encode(out)
```

`register` is an inline macro that invokes `MetaDigger` at compile time, preserving generic precision.

## 4. Compile-Time Dig (ClassMetas)

```scala
import org.beangle.commons.bean.meta.ClassMetas

// Dig ClassMeta at compile time
val cm = ClassMetas.of(classOf[User])
println(cm.properties)  // Seq[Property]
println(cm.ctors)       // Seq[Ctor]
println(cm.methods)     // Seq[Method]
```

## 5. BeanInfo Reconstruction (BeanInfo.from)

```scala
import org.beangle.commons.bean.meta.MetaModels
import org.beangle.commons.lang.reflect.BeanInfo

// Get ClassMeta from metamodel.idx
val cm = MetaModels.get(classOf[User]).get

// Reconstruct BeanInfo with MethodHandles
val bi = BeanInfo.from(cm)

// Use accessor MethodHandles
bi.getGetter("id").get.invoke(user)      // read property
bi.getSetter("name").get.invoke(user, "new name")  // write property
```

## 6. Debug JSON Export (MetaJson)

```scala
import org.beangle.commons.bean.meta.{ClassMetas, MetaJson}

val cm = ClassMetas.of(classOf[User])
println(MetaJson.toJson(cm))
```

CLI usage:
```bash
java -cp <classpath> org.beangle.commons.bean.meta.MetaJson <file.beaninfo>...
```

## 7. MetaGenerator Tool

Scans classes directory for MetaRegistry subclasses and generates metamodel.idx:

```bash
# Generate metamodel.idx
MetaGenerator target/classes

# Generate to custom path
MetaGenerator -o output.idx target/classes

# Also generate GraalVM native-image config files
MetaGenerator --graalvm target/classes
```

### GraalVM Native Image Support

With `--graalvm` option, generates:

| File | Purpose |
|------|---------|
| `reflect-config.json` | Declares classes needing reflection access |
| `resource-config.json` | Declares metamodel.idx as resource to include |

## 8. Memory Footprint

| Component | 1000 classes |
|-----------|--------------|
| ClassMeta objects | ~1.4MB |
| String objects (with dedup) | ~44KB |
| HashMap overhead | ~64KB |
| **Total** | **~1.5MB** |

- String pool dedup saves ~144KB (property names like "id", "name" stored once);
- PropertyInfo/MethodInfo hold MetaModel references, avoiding duplicate storage;
- Compared to ClassLoader loading 1000 classes (50–200MB bytecode), metamodel footprint is negligible.

## 9. Size Comparison

| Class | Properties | Binary | JSON | Ratio |
|-------|------------|--------|------|-------|
| CodecSample | 7 | 233B | 755B | 3.2× |
| CodecValue | 8 | 225B | 1,399B | 6.2× |
| CodecCollections | 4 | 173B | 743B | 4.3× |
| CodecCtor | 3 | 139B | 455B | 3.3× |
| CodecJson | 5 | 181B | 1,000B | 5.5× |
| **Total** | | **951B** | **4,352B** | **4.6×** |
