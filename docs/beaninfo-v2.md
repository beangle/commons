# Beangle Meta Binary Format v1

> **Goal**: A compact, self-contained, random-accessible binary representation for class metadata (properties / ctors / methods).
>
> **Design inspiration**: JVM class file + TASTy: **self-contained constant pool + length-prefixed sections + magic/version**;
> type information comes from **compile-time digger** (equivalent to TASTy information, but format is fully self-controlled,
> no dependency on compiler/toolchain libraries at runtime).
>
> **Self-contained blob**: each blob does not reference external data, ready for jar-level `metamodel.idx`
> (directory = class name → offset/length).

## 1. Overview

```
metamodel.idx (multi-class container)
┌───────────────────────────────────────────────┐
│ Header: magic "BMXI" + version u16            │  6B
├───────────────────────────────────────────────┤
│ Directory: count u32 + entries...             │  Random access by class name
├───────────────────────────────────────────────┤
│ Blobs: count × { self-contained v1 blob }     │  Each starts with magic "BMET"
└───────────────────────────────────────────────┘

Single class blob (MetaCodec format):
┌───────────────────────────────────────────────┐
│ Header: magic "BMET" + version u16 + flags u16│  10B
│         + nameIdx u16 (class name, pool index)│
├───────────────────────────────────────────────┤
│ Constant pool: poolSize u16 + UTF8 entries    │  All strings deduplicated
├───────────────────────────────────────────────┤
│ Sections (any order, unknown by length skip): │
│   tag=1 properties │ tag=2 ctors │ tag=3 methods
└───────────────────────────────────────────────┘
```

- **Big-endian** for all multi-byte integers (consistent with JVM class file);
- Pool index is u16, **0xFFFF = absent**;
- Class names use JVM internal format (`/` separated): `org/beangle/data/hibernate/model/User`;
- Primitives use JVM names: `int` `long` `short` `boolean` `byte` `char` `float` `double` `void`.

## 2. Magic Numbers

| Component | Magic | Version | Description |
|-----------|-------|---------|-------------|
| **MetaCodec** (single class blob) | `BMET` | 1 | Beangle Meta |
| **MetaIndex** (multi-class container) | `BMXI` | 1 | Beangle Meta Index |

## 3. Single Class Blob Header (10B)

| Field | Size | Description |
|-------|------|-------------|
| magic | 4B | `"BMET"` (0x42 0x4D 0x45 0x54) |
| version | u16 | = 1 |
| flags | u16 | Reserved, = 0 |
| nameIdx | u16 | Class name index in constant pool |

## 4. Constant Pool

```
poolSize: u16                        // Explicit entries, index 128 .. 128+poolSize-1
poolSize × { tag: u8, len: u16, bytes: UTF-8 }
```

- **Builtin type zone (fixed indices 0–127, 0 bytes)**: primitives + `java/lang/String` + `scala/Option` + boxed types (0–17) + date/time/calendar types (18–38) + Scala/Java collections (39–66) + beangle value types (67–72) + common data types (73–79) + JSON/Properties types (80–84) — **not stored in pool**;
- 85–127: **Reserved** for future builtins (adding builtins doesn't shift explicit boundary);
- Explicit entries start at index **128**;
- Each entry: `tag u8(=0)` + `len u16` + UTF-8 bytes.

## 5. TypeInfo (Dual Form)

```
Form 1 (builtin clazz < 128 AND argCount < 128):
  [1B: bit7=1 | argCount(7bit)] [1B clazzIdx]        → 2B

Form 2 (explicit clazz / argCount ≥ 128):
  [2B clazzIdx u16] [1B argCount]                    → 3B

Both followed by: argCount × TypeInfo (recursive)
```

- **No option/iterable/general distinction**: all flattened to `clazz + args`;
- **Kind restored at read time**: `TypeInfo.get(clazz, args)` determines OptionType/IterableType/GeneralType;
- **Property-level Option special case**: `Option[X]` stores **element type X** (flattened), `isOptional=true` in flags bit1.

## 6. Sections

Each section: `tag u8 + length u32 + payload`; unknown tags skipped by length (forward compatible).

### 6.1 Properties (tag=1)

```
payload: count u8, count × {
  nameIdx           u16   Property name (pool index)
  typeinfo          TypeInfo
  flags             u8    bit0=transient; bit1=isOptional; others reserved
}
```

- **Pure declaration, no accessor info**: getter/setter not stored — discovered by naming convention at BeanInfo construction time;
- **Setter param type not stored**: always equals `typeinfo.clazz`.

### 6.2 Constructors (tag=2)

```
payload: count u8, count × {
  paramCount u8,
  paramCount × {
    nameIdx     u16   Parameter name (pool index)
    typeinfo    TypeInfo
    defaultTag  u8:   0=none, 1=null, 2=boolean, 3=byte, 4=short,
                      5=int, 6=long, 7=float, 8=double, 9=char,
                      10=string(pool idx), 11=enum(pool idx)
  }
}
```

### 6.3 Methods (tag=3)

```
payload: count u16, count × {
  nameIdx       u16   Method name (pool index)
  paramCount    u8
  paramCount × paramTypeIdx u16    // Erased param type (pool index)
}
```

- **count is u16** (may exceed 255 for large classes); other counts are u8.

## 7. MetaIndex Format (metamodel.idx)

Multi-class container with directory for random access:

```
Header:    magic "BMXI" + version u16
Directory: count u32, count × { nameLen u16 + name UTF-8 + offset u32 + length u32 }
Blobs:     count × { self-contained MetaCodec blob }
```

- Directory sorted by class name;
- Each blob is self-contained (own header + pool);
- `MetaIndex.find(className)` locates single class by directory lookup.

## 8. Global Lookup (MetaModels)

`MetaModels` scans `classpath*:META-INF/beangle/metamodel.idx` at startup:

```scala
// Lookup by Class
MetaModels.get(classOf[User])  // Option[ClassMeta]

// Lookup by class name
MetaModels.get("org.example.User")  // Option[ClassMeta]
```

- Builds in-memory index: className → (URL, offset, length);
- Loads ClassMeta on demand without caching;
- Supports both dot-separated and JVM internal class names.

## 9. MetaGenerator Tool

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

Example reflect-config.json:
```json
[
  {
    "name": "com.example.User",
    "allDeclaredFields": true,
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true
  }
]
```

## 10. Architecture Summary

```
org.beangle.commons.bean.meta
├── MetaModel          # Core data model (ClassMeta, Property, Ctor, Method)
├── MetaCodec          # Binary codec for single class blob (BMET magic)
├── MetaIndex          # Multi-class container format (BMXI magic)
├── MetaModels         # Global lookup by class name
├── MetaJson           # Debug JSON export (one-way)
├── MetaRegistry       # Abstract registry for collecting ClassMeta
├── MetaGenerator      # CLI tool for generating metamodel.idx
├── ClassMetas         # Compile-time entry point (macros)
├── ClassMetaDigger    # Compile-time macro digger
└── BeanMetaConverter  # Runtime BeanInfo → ClassMeta bridge
```

## 11. Design Decisions

1. **Two magic numbers**: `BMET` for single class blob, `BMXI` for multi-class container — clear format distinction;
2. **Version = 1**: not yet released, no backward compatibility burden;
3. **Pure declaration**: properties/methods store no accessor info — discovered by naming convention;
4. **TypeInfo flattened**: no option/iterable/general tag — kind derived at read time;
5. **Property-level Option**: stores element type + isOptional flag — saves bytes;
6. **Builtin type zone**: 85 common types with fixed indices (0 bytes) — significant space savings;
7. **Self-contained blobs**: each blob has own pool — enables random access without loading other classes;
8. **Compile-time precision**: generic args keep exact types (e.g., `Map[Int, X]` key is `int`, not `Object`);
9. **Forward compatible**: unknown section tags skipped by length — minor evolution without version bump.

## 12. Size Comparison

| Class | Properties | Binary | JSON | Ratio |
|-------|------------|--------|------|-------|
| CodecSample | 7 | 233B | 755B | 3.2× |
| CodecValue | 8 | 225B | 1,399B | 6.2× |
| CodecCollections | 4 | 173B | 743B | 4.3× |
| CodecCtor | 3 | 139B | 455B | 3.3× |
| CodecJson | 5 | 181B | 1,000B | 5.5× |
| **Total** | | **951B** | **4,352B** | **4.6×** |

- Binary format achieves ~4.6× compression vs compact JSON;
- Builtin type indices eliminate ~40–80B pool overhead per class.
