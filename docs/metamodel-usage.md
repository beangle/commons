# Beangle Beanmodel Usage Guide

> Usage, architecture, and tooling for the beangle metamodel system.
> For the binary format specification, see [metamodel-spec.md](metamodel-spec.md).

## 1. Architecture

```
org.beangle.commons.bean.meta
├── MetaModel          # Core data model (ClassMeta, Property, Ctor, Method)
├── MetaCodec          # Binary codec for single class blob (BBET magic)
├── MetaIndex          # Multi-class container format (BBXI magic)
├── MetaModels         # Global lookup (full-load-at-startup)
├── MetaLoader         # Runtime reflection → ClassMeta (fallback)
├── MetaJson           # Debug JSON export (one-way)
├── MetaRegistrar       # Abstract registry for collecting ClassMeta
├── MetaGenerator      # CLI tool for generating beanmeta.idx
├── ClassMetas         # Compile-time entry point (macros)
└── MetaDigger         # Compile-time macro digger
```

### Data Flow

```
Compile time:
  MetaDigger → ClassMeta → MetaCodec.encode → binary blob

Runtime (with binary):
  beanmeta.idx → MetaModels → ClassMeta → BeanInfo.from → BeanInfo

Runtime (reflection fallback):
  Class → MetaLoader → ClassMeta → BeanInfo.from → BeanInfo
```

ClassMeta is the single source of truth. Both binary and reflection paths produce ClassMeta;
BeanInfo.from reconstructs BeanInfo with accessor MethodHandles.

## 2. Global Lookup (MetaModels)

`MetaModels` scans `classpath*:META-INF/beangle/beanmeta.idx` at startup and loads all ClassMeta into memory:

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

## 3. Compile-Time Registration (MetaRegistrar)

```scala
import org.beangle.commons.bean.meta.MetaRegistrar

class AppRegistry extends MetaRegistrar {
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

// Get ClassMeta from beanmeta.idx
val cm = MetaModels.get(classOf[User]).get

// Reconstruct BeanInfo with MethodHandles
val bi = BeanInfo.from(cm)

// Use accessor MethodHandles
bi.getGetter("id").get.invoke(user)      // read property
bi.getSetter("name").get.invoke(user, "new name")  // write property
```

## 6. BeanInfos Lookup and `$` Subclass Fallback

`BeanInfos` 是进程级入口：先查缓存，未命中时按「二进制索引（`MetaModels`）→ 运行时反射
（`MetaLoader`）」两级加载。对 **`$` 子类**（框架/编译器生成的子类）有专门处理，直接复用
父类的 BeanMeta，而不是反射子类本身：

- **Hibernate 懒加载代理**：`Entity$HibernateProxy`，类名前缀与父类全名一致；
- **Scala 3 枚举值类**：`NoticeStatus$$anon$1`，每个带参 case 编译成匿名子类，名字含 `$$`。

命中时把父类 meta 绑定到子类（`copy(clazz = 子类, ctors = Seq.empty)`）。这类子类没有自有
bean 属性，仅继承父类，因此可以直接复用父类元数据：

```scala
import org.beangle.commons.lang.reflect.BeanInfos

enum NoticeStatus(val title: String) {
  case Draft extends NoticeStatus("草稿")
  case Passed extends NoticeStatus("审核通过")
}

// 值类（NoticeStatus$$anon$N）不能直接反射，BeanInfos 自动回退到 NoticeStatus 的 meta
val bi = BeanInfos.get(NoticeStatus.Passed.getClass)
bi.properties.contains("title")                          // true
bi.getGetter("title").get.invoke(NoticeStatus.Passed)    // "审核通过"

// Hibernate 代理同理：拿到的是 User 的属性表
val proxy = session.load(classOf[User], 1L)              // 实际类型 User$HibernateProxy
BeanInfos.get(proxy.getClass).properties
```

> **native image 注意**：回退路径下 `BeanInfo.from` 仍会对子类调用 `getMethods` 绑定
> MethodHandle，因此需保证父类（及值类）已登记 public 方法元数据。Scala 3 枚举经
> `registerEnum`（`AotPolicy.enumPolicy`）自动覆盖枚举类、伴生对象与全部值类；Hibernate
> 代理随实体类注册（`AotPolicy.bean`）覆盖。详见 [aot-usage.md](aot-usage.md)。

## 7. Debug JSON Export (MetaJson)

```scala
import org.beangle.commons.bean.meta.{ClassMetas, MetaJson}

val cm = ClassMetas.of(classOf[User])
println(MetaJson.toJson(cm))
```

CLI usage:
```bash
java -cp <classpath> org.beangle.commons.bean.meta.MetaJson <file.beaninfo>...
```

## 8. MetaGenerator Tool

Scans classes directory for MetaRegistrar subclasses and generates beanmeta.idx:

```bash
# Generate beanmeta.idx
MetaGenerator target/classes

# Generate to custom path
MetaGenerator -o output.idx target/classes
```

## 9. AotHintGenerator (GraalVM Native Image)

For detailed AOT/native-image usage, see [aot-usage.md](aot-usage.md).

Quick start — enable the SBT plugin:

```scala
lazy val myModule = (project in file(".")).enablePlugins(AotPlugin)
```

This scans compiled classes for `AotHintRegistrar` implementations and generates
GraalVM configs as managed resources automatically.

## 10. Memory Footprint

| Component | 1000 classes |
|-----------|--------------|
| ClassMeta objects | ~1.4MB |
| String objects (with dedup) | ~44KB |
| HashMap overhead | ~64KB |
| **Total** | **~1.5MB** |

- String pool dedup saves ~144KB (property names like "id", "name" stored once);
- PropertyInfo/MethodInfo hold MetaModel references, avoiding duplicate storage;
- Compared to ClassLoader loading 1000 classes (50–200MB bytecode), metamodel footprint is negligible.

## 11. Size Comparison

| Class | Properties | Binary | JSON | Ratio |
|-------|------------|--------|------|-------|
| CodecSample | 7 | 233B | 755B | 3.2× |
| CodecValue | 8 | 225B | 1,399B | 6.2× |
| CodecCollections | 4 | 173B | 743B | 4.3× |
| CodecCtor | 3 | 139B | 455B | 3.3× |
| CodecJson | 5 | 181B | 1,000B | 5.5× |
| **Total** | | **951B** | **4,352B** | **4.6×** |
