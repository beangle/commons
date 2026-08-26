# Beangle JSON Usage Guide

> Creating, querying and updating JSON documents with `org.beangle.commons.json`.

## 1. Architecture

```
org.beangle.commons.json
├── Json              # Entry point: parse / of / resolvePath / deepCopy / toJson
├── JsonObject        # JSON object node (mutable, key-value based)
├── JsonArray         # JSON array node (mutable, index based)
├── JsonValue         # Leaf node wrapping a primitive value
├── Null              # JSON null value
├── JsonParser        # Low-level parser (java.io.Reader based)
├── JsonMapper        # Trait for custom type <-> JsonObject mapping
└── JsonConverter     # String -> Json conversion (org.beangle.commons.conversion.string)
```

`JsonObject` and `JsonArray` are mutable; `query` / `update` accept a path expression and
operate directly on the node tree. Every node implements the `Json` trait
(`query`, `get`, `children`, `toJson`, `value`).

## 2. Creating and Parsing

### 2.1 Parse from text

```scala
import org.beangle.commons.json.*

val obj = Json.parseObject("""{"name":"jack","age":30}""")   // JsonObject
val arr = Json.parseArray("""[1,2,3]""")                    // JsonArray
val any = Json.parse("""{"ok":true}""")                     // Json or JsonValue
```

Parsed numbers follow these rules:
- integers are `Int` (a value overflowing `Int` falls back to the raw string);
- decimals (`.` or exponent) are `Double`;
- `true` / `false` are `Boolean`; `null` becomes `Null` (reported as `None` by getters).

### 2.2 Convert from native values

`Json.of` recursively converts Scala/Java maps, collections and arrays into the JSON tree:

```scala
import java.util as ju

val m = new ju.HashMap[String, Any]()
m.put("name", "jack")
m.put("tags", ju.List.of("a", "b"))
val json = Json.of(m)          // JsonObject
json.toJson                    // {"name":"jack","tags":["a","b"]}
```

### 2.3 Build manually

```scala
val user = JsonObject("id" -> 1, "name" -> "jack")
user.add("grade", List("math" -> 90, "english" -> 80))   // List of tuples -> JsonObject
user.add("tags", List("a", "b"))                         // other Iterable -> JsonArray
user.add("age", null)                                    // null removes the key
user.addAll(Map("city" -> "Shanghai"))

val arr = JsonArray(1, "two", true)
Json.emptyObject   // {}
Json.emptyArray    // []
```

Note: `Long` values are serialized as JSON strings (`"3"`), while `Int` is serialized
as a number. Use `Long` intentionally, or convert with `getInt` / `getLong` when reading.

## 3. Querying (查找)

### 3.1 Path expression

`query(path)` returns `Option[Any]`. The path is a sequence of property names and array
indices separated by `/`, `.` or `[]`. All of the following forms are equivalent and
no leading slash is required:

```scala
val text = """{"choices":[{"message":{"role":"assistant","content":"hello"}}]}"""
val obj = Json.parseObject(text)

obj.query("choices/0/message/content")   // Some("hello")  (slash style)
obj.query("/choices/0/message/content")  // Some("hello")  (leading slash)
obj.query("choices.0.message.content")   // Some("hello")  (dot style)
obj.query("choices[0].message[content]") // Some("hello")  (bracket style)
obj.query("choices/0/message/role")      // Some("assistant")
obj.query("choices/9/message")           // None (index out of bounds)
obj.query("missing")                     // None
```

`JsonArray.query` works the same way, resolving from the array root:

```scala
val arr = Json.parseArray("""[{"a":{"b":1}},{"a":{"b":2}}]""")
arr.query("1/a/b")   // Some(2)
arr.query("0/a/b")   // Some(1)
```

### 3.2 Wildcard projection

`*` projects over every element of an array, returning a `JsonArray` of the projected values:

```scala
val roles = Json.parseObject("""{"roles":[{"name":"a"},{"name":"b"}]}""")
roles.query("roles[*].name").get.asInstanceOf[JsonArray].toJson   // ["a","b"]
```

### 3.3 Navigation and typed accessors

```scala
val obj = Json.parseObject("""{"name":"jack","age":30,"meta":{"ok":true},"tags":["x","y"]}""")

obj \ "meta" \ "ok"              // Json navigation, missing path yields empty JsonObject
obj.get("age")                   // Option[Any] = Some(30)
obj("age")                       // Any = 30, throws NoSuchElementException if missing

obj.getString("name")            // "jack"
obj.getString("name", "default") // explicit default
obj.getBoolean("meta.ok", false) // path lookup not supported here, use query
obj.getInt("age")                // 30
obj.getLong("id", 0L)
obj.getDouble("score", 0d)
obj.getDate("birthday")          // java.time.LocalDate via TemporalConverter
obj.getDateTime("createdAt")     // java.time.LocalDateTime
obj.getInstant("ts")             // java.time.Instant
obj.getObject("meta")            // JsonObject, or a new empty one when missing
obj.getArray("tags")             // JsonArray, or an empty one when missing
```

Typed getters accept a `defaultValue` when the key is absent. They are key-based (single
level); for nested values use `query` and cast, or combine with `\`.

### 3.4 Subset matching

`isMatch` checks whether this object contains all keys/values of the target (deep,
order-insensitive for objects):

```scala
val src = Json.parseObject("""{"id":1,"name":"jack","tags":["a","b"]}""")
src.isMatch(Json.parseObject("""{"name":"jack"}"""))     // true
src.isMatch(Json.parseObject("""{"name":"mike"}"""))     // false
src.isMatch(Json.parseObject("""{"tags":["a","b"]}"""))  // true
```

## 4. Updating (更新)

### 4.1 Update by path

`update(path, value)` creates intermediate objects/arrays on demand and returns `this`
for chaining:

```scala
val json = new JsonObject()
json.update("query/term/std/0/skills/0/name", "Play Basketball")
json.toJson
// {"query":{"term":{"std":[{"skills":[{"name":"Play Basketball"}]}]}}}
```

`update` is strict: if the path cannot be resolved/created under the current rules it
throws `IllegalArgumentException` instead of silently ignoring the call.

```scala
json.update("roles.name", "guest")        // throws: array must be indexed, e.g. roles[0].name
json.update("profile[*].name", "guest")   // throws: wildcard only applies to arrays
json.update("roles[abc].name", "guest")   // throws: illegal index token
```

### 4.2 Array index updates

```scala
val data = new JsonObject()
data.update("jobs/0/title", "Manager")   // auto-creates the array slot
data.update("jobs/1/title", "Director")  // out-of-range positive index auto-expands (null-filled)
data.update("jobs[-1]/title", "Boss")    // negative index: last element
```

Negative indexes never auto-create slots: `roles[-3].name` on a 2-element array throws.
An out-of-range positive index expands the array (padding with `null`), and a path that
would traverse a `null` slot fails with `IllegalArgumentException`.

### 4.3 Wildcard batch updates

`*` applies the update to every element of the array level:

```scala
val roles = Json.parseObject("""{"roles":[{"name":"role1"},{"name":"role2"}]}""")
roles.update("roles[*].name", "guest")
roles.toJson   // {"roles":[{"name":"guest"},{"name":"guest"}]}

val matrix = Json.parseObject("""{"matrix":[[1,2],[3,4]]}""")
matrix.update("matrix[*][*]", 0)
matrix.toJson  // {"matrix":[[0,0],[0,0]]}
```

### 4.4 Removing keys

```scala
val obj = JsonObject("a" -> 1, "b" -> 2, "c" -> 3)
obj.remove("a", "b")      // {"c":3}
obj - "c"                 // removes one key, returns this
obj.add("x", null)        // null value removes the key
```

## 5. Serialization

```scala
val json = JsonObject("name" -> "jack", "age" -> 30, "ok" -> true, "nil" -> None)
json.toJson          // {"name":"jack","age":30,"ok":true,"nil":null}
json.toString        // same as toJson

Json.escape("a\"b\nc")   // quoted, escaped string literal
Json.toJson(Map("a" -> 1))            // {"a":1}
Json.toJson(List(1, 2))               // [1,2]
Json.deepCopy(json)                   // deep, independent copy of the tree
```

## 6. Conversion with other beangle modules

- `JsonConverter` registers `String -> JsonObject / JsonArray / Json` in the string
  converter factory, so conversion frameworks can materialize JSON from text.
- Implement `JsonMapper[T]` (`toJson(obj): JsonObject`, `fromJson(obj): JsonObject`)
  for explicit custom type mappings.

## 7. End-to-end example

Querying and updating an OpenAI-style chat completion response:

```scala
val response =
  """{"created":1700000000,"usage":{"completion_tokens":48,"prompt_tokens":256,
     |"prompt_cache_hit_tokens":200,"prompt_cache_miss_tokens":56},
     |"model":"demo-chat","id":"chatcmpl-demo01","system_fingerprint":"fp_demo01",
     |"choices":[{"finish_reason":"stop","index":0,
     |  "message":{"role":"assistant","content":"alpha\nbeta\ngamma"},
     |  "logprobs":null}],"object":"chat.completion"}""".stripMargin

val json = Json.parseObject(response)

// lookup
json.getString("model")                              // "demo-chat"
json.query("choices/0/message/content")              // Some("alpha\nbeta\ngamma")
json.query("usage/prompt_cache_hit_tokens")          // Some(200)
json.query("choices[0].finish_reason")               // Some("stop")

// update
json.update("choices/0/message/content", "replaced")
json.update("usage/completion_tokens", 99)
json.remove("system_fingerprint")

json.toJson   // the modified document
```

Practical tips:
- Use `/`-separated paths (e.g. `choices/0/message/content`) for readability; `/`,
  `.` and `[]` are interchangeable and no leading slash is required.
- `query` returns `Option`; use typed getters (`getString`, `getInt`, ...) for
  single-level keys, and `query` for nested paths.
- `update` auto-creates missing objects/arrays, but stays strict about illegal paths —
  index non-numeric tokens, wildcards on objects, and unresolvable negative indexes
  throw `IllegalArgumentException`.
