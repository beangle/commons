# Beangle Metamodel Binary Specification

> Compact, self-contained, random-accessible binary format for class metadata (properties / ctors / methods).

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

### 4.1 Builtin Type Index Table

| Index | Type | Index | Type |
|-------|------|-------|------|
| 0 | int | 1 | long |
| 2 | short | 3 | boolean |
| 4 | byte | 5 | char |
| 6 | float | 7 | double |
| 8 | java/lang/String | 9 | scala/Option |
| 10–17 | Boxed primitives | 18–38 | Date/Time/Calendar |
| 39–66 | Scala/Java collections | 67–72 | Beangle value types |
| 73–79 | Common data types | 80–84 | JSON/Properties |
| 85–127 | Reserved | 128+ | Explicit pool |

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
- **Property-level Option special case**: `Option[X]` stores **element type X** (flattened), `isOptional=true` in flags bit1;
- **Big-endian Form 1 collision-free**: Form 2 high byte is 0x00–0x7F for idx < 0x8000, never collides with Form 1's bit7=1.

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

- **Pure declaration, no accessor info**: getter/setter not stored — discovered by naming convention;
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
  paramCount × TypeInfo    // Parameter types with full generic precision
}
```

- **count is u16** (may exceed 255 for large classes); other counts are u8;
- **Parameter types stored as TypeInfo** — preserves generic precision (e.g., `Map[Int, String]` key is `int`, not `Object`).

## 7. MetaIndex Format (metamodel.idx)

```
Header:    magic "BMXI" + version u16
Directory: count u32, count × { nameLen u16 + name UTF-8 + offset u32 + length u32 }
Blobs:     count × { self-contained MetaCodec blob }
```

- Directory sorted by class name;
- Each blob is self-contained (own header + pool);
- `MetaIndex.find(className)` locates single class by directory lookup.

## 8. Byte-Level Example (User class, 3 properties)

> Simplified example with 3 representative properties (primitive / reference / option wrapper).
> Actual encoding is 123B. Each line is 16 bytes, hex offset on the left.

```scala
class User(var id: Long, var name: String, var nick: Option[String])
// 3 properties: id: Long, name: String, nick: Option[String]
// var → scalac generates getter id()/name()/nick() and setter id_$eq/name_$eq/nick_$eq
// primary ctor 3 params (no defaults); no other public methods → methods count=0
```

```
Offset  Hex bytes                                                       ASCII
0000    42 4D 45 54 00 01 00 00 00 80 00 04 00 00 25 6F  |BMET..........%o|
0010    72 67 2F 62 65 61 6E 67 6C 65 2F 64 61 74 61 2F  |rg/beangle/data/|
0020    68 69 62 65 72 6E 61 74 65 2F 6D 6F 64 65 6C 2F  |hibernate/model/|
0030    55 73 65 72 00 00 02 69 64 00 00 04 6E 61 6D 65  |User...id...name|
0040    00 00 04 6E 69 63 6B 01 00 00 00 10 03 00 81 80  |...nick.........|
0050    01 00 00 82 80 08 00 00 83 80 08 02 02 00 00 00  |................|
0060    13 01 03 00 81 80 01 00 00 82 80 08 00 00 83 81  |................|
0070    09 80 08 00 03 00 00 00 02 00 00                 |...........|
```

### 8.1 Header (offset 0x00–0x09, 10B)

```
42 4D 45 54 | 00 01 | 00 00 | 00 80
```

| Bytes | Value | Meaning |
|-------|-------|---------|
| `42 4D 45 54` | `"BMET"` | magic (0x42 0x4D 0x45 0x54) |
| `00 01` | version = 1 | big-endian u16 |
| `00 00` | flags = 0 | reserved |
| `00 80` | nameIdx = 128 | class name = first explicit pool entry (after builtin zone 0–127) |

### 8.2 Constant Pool (offset 0x0A)

```
00 04                     poolSize = 4 (explicit entries, u16; builtin zone 0–127 not in pool)
```

| Pool idx | Offset | Entry bytes | String |
|----------|--------|-------------|--------|
| 128 | 0x0C | `00` `00 25` + 37B | `org/beangle/data/hibernate/model/User` |
| 129 | 0x34 | `00` `00 02` + 2B | `id` |
| 130 | 0x39 | `00` `00 04` + 4B | `name` |
| 131 | 0x40 | `00` `00 04` + 4B | `nick` |

- Each entry = `tag u8(=0)` + `len u16` + UTF-8 bytes. E.g. pool[128]: `00 | 00 25 | 6F 72 67 2F ...` (class name, 37B).
- **Builtin types not in pool**: `long` = fixed index 1, `java/lang/String` = 8, `scala/Option` = 9 — saves 41B pool overhead.
- **No accessor names**: `id_$eq`/`name_$eq`/`nick_$eq` not in pool — binary only stores declarations.
- Pool end = 0x0C + Σ(3+len) = 0x47, next section starts at 0x47.

### 8.3 Properties Section (offset 0x47, tag=1, 16B payload)

```
01 | 00 00 00 10 | <16B payload>
tag    length u32   payload follows
```

Section header: `tag u8 + length u32` = 5 bytes. `01` = properties tag; `00 00 00 10` = u32 length = 16.

Payload starts with `03` = 3 properties (u8), then 3 records (each = nameIdx + TypeInfo + flags).

**Record 1: `id` (5B)**

```
00 81 | 80 01 | 00
name    typeinfo   flags
```

| Bytes | Value | Meaning |
|-------|-------|---------|
| `00 81` | nameIdx = 129 | property name `id` (pool[129]) |
| `80` | Form 1: bit7=1, argCount=0 | builtin TypeInfo fast path |
| `01` | clazzIdx = 1 | `long` (builtin index) |
| `00` | flags = 0 | not transient, not optional |

**Record 2: `name` (5B)**

```
00 82 | 80 08 | 00
```

- nameIdx = 130 (`name`); TypeInfo: clazzIdx=8 (builtin `java/lang/String`), argCount=0; flags=0.

**Record 3: `nick` (5B, option property)**

```
00 83 | 80 08 | 02
```

- nameIdx = 131 (`nick`);
- TypeInfo: stores **element type** `String` (clazzIdx=8, argCount=0) — Option wrapper not stored;
- flags = `02`: bit0=0 not transient; **bit1=1 isOptional** → reconstruct as `OptionType(String)`.

### 8.4 Constructors Section (offset 0x5C, tag=2, 19B payload)

```
02 | 00 00 00 13 | <19B payload>
tag    length u32   payload follows
```

Payload: `01` (1 ctor, u8) → `03` (3 params, u8) → each param = nameIdx + TypeInfo + defaultTag:

```
00 81 | 80 01 | 00       param id:   name=129, clazz=long args=0, defaultTag=0 (no default)
00 82 | 80 08 | 00       param name: name=130, clazz=String args=0, defaultTag=0
00 83 | 81 09 80 08 | 00  param nick: name=131, clazz=scala/Option(9) args=[String(8)], defaultTag=0
```

- Param nick's Option stored as **flattened clazz+args** (`scala/Option` builtin 9 + element `String` builtin 8; argCount=1 u8);
- With defaults: e.g. `name: String = "default"` → `00 0A <pool idx>` (tag=10 string).

### 8.5 Methods Section (offset 0x74, tag=3, 2B payload)

```
03 | 00 00 00 02 | 00 00
tag    length u32   count=0
```

`00 00` = count 0 (u16): no non-accessor public methods.

### 8.6 Size Breakdown

| Part | Bytes |
|------|-------|
| Header | 10 |
| Pool size | 2 |
| Pool entries | 59 |
| Properties section | 21 |
| Constructors section | 24 |
| Methods section | 7 |
| **Total** | **123** |

- Single property record: 5B
- Single ctor param: 5–7B (builtin TypeInfo Form 1 saves 1B)
- Builtin type indices save ~41B pool overhead
