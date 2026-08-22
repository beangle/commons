/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.bean.meta

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import java.nio.charset.StandardCharsets
import org.beangle.commons.bean.meta.MetaModel.{ClassMeta, Ctor, Method, Param, Property}
import org.beangle.commons.lang.Enums
import org.beangle.commons.lang.reflect.TypeInfo

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

/** Binary codec for [[MetaModel.ClassMeta]] (v2 format).
  *
  * A single class's meta model (properties / ctors / methods) is serialized into a
  * self-contained binary blob: header + string constant pool + length-prefixed sections.
  * Design borrows from the JVM class file: all strings are interned in the pool and
  * referenced by u16 index (0xFFFF = none); unknown sections are skipped by length.
  * Class names use JVM internal names ('/' separated); primitives use JVM names
  * (int/long/...), restored by [[classFor]].
  *
  * Input is the reflection-free [[MetaModel.ClassMeta]] (produce it from a BeanInfo
  * via [[BeanMetaConverter.from]]); decoding yields a [[MetaModel.ClassMeta]] without
  * any Method resolution. Constructing the actual BeanInfo from parsed data is
  * deferred to a future reverse-construction via `BeanInfo.Builder`.
  *
  * Precision: generic args keep the exact compile-time types (e.g. Map[Int, X] key is
  * "int", not "java.lang.Object"), so the content MUST be produced from the compile-time
  * digger ([[org.beangle.commons.lang.reflect.BeanInfos.of]]) rather than runtime reflection.
  */
object MetaCodec {

  private val Magic = "BNI2"
  private val Version = 2
  private val NoneIdx = 0xFFFF
  private val MaxPool = 0x8000 // explicit indices must be < 0x8000 (typeinfo Form-1 bit7 marker must not collide)

  // builtin type names with fixed pool indices (no pool bytes).
  // Indices 0..BuiltinBoundary-1 are the reserved builtin zone: 0..Builtins.size-1 are
  // currently used, the rest is reserved for future builtins; explicit pool entries
  // start at BuiltinBoundary (adding a builtin never shifts the explicit boundary).
  private val BuiltinBoundary = 128
  private object Builtins {
    val names: Array[String] = Array(
      // primitives + String + Option
      "int", "long", "short", "boolean", "byte", "char", "float", "double",
      "java/lang/String", "scala/Option",
      // boxed primitives
      "java/lang/Integer", "java/lang/Long", "java/lang/Boolean", "java/lang/Double", "java/lang/Float",
      "java/lang/Short", "java/lang/Byte", "java/lang/Character",
      // date / sql
      "java/util/Date", "java/sql/Date", "java/sql/Timestamp", "java/sql/Time",
      // calendar / locale
      "java/util/Calendar", "java/util/GregorianCalendar", "java/util/TimeZone", "java/util/Locale",
      // java.time
      "java/time/LocalDate", "java/time/LocalDateTime", "java/time/LocalTime", "java/time/Instant",
      "java/time/ZonedDateTime", "java/time/OffsetDateTime", "java/time/Duration", "java/time/Period",
      "java/time/YearMonth", "java/time/Year", "java/time/MonthDay", "java/time/Month", "java/time/DayOfWeek",
      // scala immutable collections
      "scala/collection/immutable/List", "scala/collection/immutable/Seq", "scala/collection/immutable/Set",
      "scala/collection/immutable/Map", "scala/collection/immutable/Vector", "scala/collection/immutable/IndexedSeq",
      "scala/collection/immutable/HashSet", "scala/collection/immutable/HashMap",
      // scala mutable collections
      "scala/collection/mutable/Map", "scala/collection/mutable/Set", "scala/collection/mutable/Buffer",
      "scala/collection/mutable/Seq", "scala/collection/mutable/ArrayBuffer", "scala/collection/mutable/ListBuffer",
      "scala/collection/mutable/HashMap", "scala/collection/mutable/HashSet",
      // scala collection root interfaces
      "scala/collection/Seq", "scala/collection/Set", "scala/collection/Map",
      "scala/collection/Iterable", "scala/collection/IndexedSeq",
      // java.util collections
      "java/util/List", "java/util/Set", "java/util/Map", "java/util/Collection",
      "java/util/ArrayList", "java/util/HashSet", "java/util/HashMap",
      // beangle value types (time / math)
      "org/beangle/commons/lang/time/WeekTime", "org/beangle/commons/lang/time/HourMinute",
      "org/beangle/commons/lang/time/CycleTime", "org/beangle/commons/lang/time/WeekDay",
      "org/beangle/commons/lang/math/Decimal5", "org/beangle/commons/lang/math/TinyDecimal5",
      // other common data-model types
      "java/math/BigDecimal", "java/math/BigInteger", "java/util/UUID", "java/lang/Object",
      "scala/math/BigDecimal", "scala/math/BigInt", "java/lang/Number",
      // properties / json value types
      "java/util/Properties",
      "org/beangle/commons/json/Json", "org/beangle/commons/json/JsonObject",
      "org/beangle/commons/json/JsonArray", "org/beangle/commons/json/JsonValue")
    private val byName: Map[String, Int] = names.zipWithIndex.toMap
    def size: Int = names.length
    def indexOf(name: String): Option[Int] = byName.get(name)
  }

  // typeinfo: flattened (clazzIdx + argCount + args), kinds derived via TypeInfo.get at read time
  // section tags
  private val SProperties = 1
  private val SCtors = 2
  private val SMethods = 3
  // ctor default value tags
  private val DNone = 0
  private val DNull = 1
  private val DBool = 2
  private val DByte = 3
  private val DShort = 4
  private val DInt = 5
  private val DLong = 6
  private val DFloat = 7
  private val DDouble = 8
  private val DChar = 9
  private val DString = 10
  private val DEnum = 11

  /** Encodes a ClassMeta into the v2 binary format. */
  def encode(cm: ClassMeta): Array[Byte] = {
    val pool = new StringPool
    // pass 1: collect all strings
    pool.index(typeName(cm.clazz))
    cm.properties.sortBy(_.name) foreach { p =>
      pool.index(p.name)
      collectType(p.typeinfo, pool) // option 属性已是元素类型（BeanMetaConverter 剥离），isOptional 在 flags
    }
    cm.ctors foreach { c =>
      c.parameters foreach { param =>
        pool.index(param.name); collectType(param.typeinfo, pool)
        param.defaultValue match {
          case Some(x: String) => pool.index(x)
          case Some(x: Enum[_]) => pool.index(x.name())
          case _ =>
        }
      }
    }
    cm.methods foreach { m =>
      pool.index(m.name)
      m.paramTypes foreach pool.typeIndex
    }
    // pass 2: write header, pool and sections
    val out = new ByteArrayOutputStream()
    val d = new DataOutputStream(out)
    d.writeBytes(Magic)
    d.writeShort(Version)
    d.writeShort(0) // flags, reserved
    d.writeShort(pool.index(typeName(cm.clazz))) // nameIdx
    d.writeShort(pool.size)
    pool.strings foreach { s =>
      val bytes = s.getBytes(StandardCharsets.UTF_8)
      d.writeByte(0); d.writeShort(bytes.length); d.write(bytes)
    }
    writeSection(d, SProperties) { dd =>
      val props = cm.properties.sortBy(_.name)
      writeCount(dd, props.size)
      props foreach (p => writeProperty(dd, p, pool))
    }
    writeSection(d, SCtors) { dd =>
      writeCount(dd, cm.ctors.size)
      cm.ctors foreach (c => writeCtor(dd, c, pool))
    }
    writeSection(d, SMethods) { dd =>
      dd.writeShort(cm.methods.size) // methods count 保持 u16（大型类方法数留余量）
      cm.methods foreach (m => writeMethod(dd, m, pool))
    }
    d.flush()
    out.toByteArray
  }

  /** Parses a v2 binary blob into a reflection-free [[MetaModel.ClassMeta]].
    *
    * Only classes and types are resolved (TypeInfo carries Class references);
    * getter/setter/method are kept as raw pool strings, no Method resolution
    * happens. Constructing a BeanInfo from the result is future work
    * (reverse-construction via `BeanInfo.Builder`), not provided here.
    */
  def parse(bytes: Array[Byte]): ClassMeta = {
    val in = new DataInputStream(new ByteArrayInputStream(bytes))
    val magic = new Array[Byte](4)
    in.readFully(magic)
    if (!new String(magic, StandardCharsets.US_ASCII).equals(Magic))
      throw new IllegalArgumentException("Not a beaninfo v2 binary")
    val version = in.readUnsignedShort()
    if version != Version then throw new IllegalArgumentException(s"Unsupported beaninfo version $version,expected $Version")
    in.readUnsignedShort() // flags, reserved
    val selfIdx = in.readUnsignedShort()
    if selfIdx == NoneIdx then throw new IllegalArgumentException("Missing clazz in beaninfo header")
    val poolSize = in.readUnsignedShort()
    if poolSize == 0 then throw new IllegalArgumentException("Empty string pool")
    val pool = new Array[String](poolSize)
    var i = 0
    while i < poolSize do
      val tag = in.readUnsignedByte()
      if tag != 0 then throw new IllegalArgumentException(s"Unsupported pool entry tag $tag")
      val len = in.readUnsignedShort()
      val b = new Array[Byte](len)
      in.readFully(b)
      pool(i) = new String(b, StandardCharsets.UTF_8)
      i += 1
    val clazz = classFor(nameOf(selfIdx, pool))
    var properties = Seq.empty[Property]
    var ctors = Seq.empty[Ctor]
    var methods = Seq.empty[Method]
    while in.available() > 0 do
      val tag = in.readUnsignedByte()
      val len = in.readInt()
      val payload = new Array[Byte](len)
      in.readFully(payload)
      val pin = new DataInputStream(new ByteArrayInputStream(payload))
      tag match
        case SProperties => properties = readProperties(pin, pool)
        case SCtors => ctors = readCtors(pin, pool)
        case SMethods => methods = readMethods(pin, pool)
        case _ => // skip unknown section
    ClassMeta(clazz, properties, ctors, methods)
  }

  private def writeSection(out: DataOutputStream, tag: Int)(write: DataOutputStream => Unit): Unit = {
    val buf = new ByteArrayOutputStream()
    val dd = new DataOutputStream(buf)
    write(dd)
    dd.flush()
    out.writeByte(tag)
    out.writeInt(buf.size())
    buf.writeTo(out)
  }

  private def writeProperty(d: DataOutputStream, p: Property, pool: StringPool): Unit = {
    d.writeShort(pool.index(p.name))
    writeType(d, p.typeinfo, pool) // option 属性已是元素类型
    val flags = (if p.isTransient then 1 else 0) | (if p.isOptional then 2 else 0)
    d.writeByte(flags)
  }

  private def writeCtor(d: DataOutputStream, c: Ctor, pool: StringPool): Unit = {
    writeCount(d, c.parameters.size)
    c.parameters foreach { param =>
      d.writeShort(pool.index(param.name))
      writeType(d, param.typeinfo, pool)
      writeDefault(d, param.defaultValue, pool)
    }
  }

  private def writeMethod(d: DataOutputStream, m: Method, pool: StringPool): Unit = {
    d.writeShort(pool.index(m.name))
    d.writeByte(m.paramTypes.size)
    m.paramTypes foreach (t => d.writeShort(pool.typeIndex(t)))
  }

  /** Flattened TypeInfo with a builtin fast form (big-endian friendly: high byte bit7 marker).
    *
    * Form 1 (builtin clazz < 128 AND argCount < 128):
    *   [1B: bit7=1 | argCount(7bit)] [1B clazzIdx]        → 2B
    * Form 2 (explicit / argCount >= 128):
    *   [2B clazzIdx u16] [1B argCount]                    → 3B
    * A u16 explicit index's high byte is 0x00–0x7F for idx < 0x8000 (guarded), so bit7
    * never collides under big-endian; kinds are re-derived at read via TypeInfo.get.
    */
  private def writeType(d: DataOutputStream, ti: TypeInfo, pool: StringPool): Unit = {
    val idx = pool.typeIndex(typeName(ti.clazz))
    if idx < 128 && ti.args.size < 128 then
      d.writeByte(0x80 | ti.args.size)
      d.writeByte(idx)
    else
      d.writeShort(idx)
      writeCount(d, ti.args.size)
    ti.args foreach (writeType(d, _, pool))
  }

  /** Count fields are u8 (member counts are small; overflow is a format error, writeByte would silently truncate). */
  private def writeCount(d: DataOutputStream, n: Int): Unit = {
    if n > 255 then throw new IllegalArgumentException(s"Count overflow (max 255): $n")
    d.writeByte(n)
  }

  private def writeDefault(d: DataOutputStream, v: Option[Any], pool: StringPool): Unit = v match
    case None => d.writeByte(DNone)
    case Some(null) => d.writeByte(DNull)
    case Some(x: java.lang.Boolean) => d.writeByte(DBool); d.writeBoolean(x.booleanValue)
    case Some(x: java.lang.Byte) => d.writeByte(DByte); d.writeByte(x.byteValue)
    case Some(x: java.lang.Short) => d.writeByte(DShort); d.writeShort(x.shortValue)
    case Some(x: java.lang.Integer) => d.writeByte(DInt); d.writeInt(x.intValue)
    case Some(x: java.lang.Long) => d.writeByte(DLong); d.writeLong(x.longValue)
    case Some(x: java.lang.Float) => d.writeByte(DFloat); d.writeFloat(x.floatValue)
    case Some(x: java.lang.Double) => d.writeByte(DDouble); d.writeDouble(x.doubleValue)
    case Some(x: java.lang.Character) => d.writeByte(DChar); d.writeChar(x.charValue)
    case Some(x: String) => d.writeByte(DString); d.writeShort(pool.index(x))
    case Some(x: Enum[_]) => d.writeByte(DEnum); d.writeShort(pool.index(x.name()))
    case Some(_) => d.writeByte(DNone) // unsupported default value, dropped

  private def collectType(ti: TypeInfo, pool: StringPool): Unit = {
    pool.typeIndex(typeName(ti.clazz))
    ti.args foreach (collectType(_, pool))
  }

  private def readProperties(in: DataInputStream, pool: Array[String]): Seq[Property] = {
    val count = in.readUnsignedByte()
    val out = new Array[Property](count)
    var i = 0
    while i < count do
      val name = nameOf(in.readUnsignedShort(), pool)
      val ti = readType(in, pool)
      val flags = in.readUnsignedByte()
      out(i) = Property(name, ti, (flags & 1) != 0, (flags & 2) != 0)
      i += 1
    out.toSeq
  }

  private def readCtors(in: DataInputStream, pool: Array[String]): Seq[Ctor] = {
    val count = in.readUnsignedByte()
    val out = new Array[Ctor](count)
    var i = 0
    while i < count do
      val pcount = in.readUnsignedByte()
      val params = new Array[Param](pcount)
      var j = 0
      while j < pcount do
        val name = nameOf(in.readUnsignedShort(), pool)
        val ti = readType(in, pool)
        val dv = readDefault(in, pool, ti.clazz)
        params(j) = Param(name, ti, dv)
        j += 1
      out(i) = Ctor(ArraySeq.from(params))
      i += 1
    out.toSeq
  }

  private def readMethods(in: DataInputStream, pool: Array[String]): Seq[Method] = {
    val count = in.readUnsignedShort()
    val out = new Array[Method](count)
    var i = 0
    while i < count do
      val name = nameOf(in.readUnsignedShort(), pool)
      val n = in.readUnsignedByte()
      val params = new Array[String](n)
      var j = 0
      while j < n do
        params(j) = nameOf(in.readUnsignedShort(), pool); j += 1
      out(i) = Method(name, ArraySeq.from(params))
      i += 1
    out.toSeq
  }

  /** Reads the flattened TypeInfo (Form 1 builtin fast form / Form 2 explicit);
    * kind (Option / Iterable / General) is derived by TypeInfo.get from the clazz.
    */
  private def readType(in: DataInputStream, pool: Array[String]): TypeInfo = {
    val first = in.readUnsignedByte()
    val (clazz, n) =
      if (first & 0x80) != 0 then
        (classFor(nameOf(in.readUnsignedByte(), pool)), first & 0x7F)
      else
        val lo = in.readUnsignedByte()
        (classFor(nameOf((first << 8) | lo, pool)), in.readUnsignedByte())
    TypeInfo.get(clazz, ArraySeq.from((0 until n).map(_ => readType(in, pool))))
  }

  private def readDefault(in: DataInputStream, pool: Array[String], paramClazz: Class[_]): Option[Any] = {
    in.readUnsignedByte() match
      case DNone => None
      case DNull => Some(null)
      case DBool => Some(in.readBoolean())
      case DByte => Some(in.readByte())
      case DShort => Some(in.readShort())
      case DInt => Some(in.readInt())
      case DLong => Some(in.readLong())
      case DFloat => Some(in.readFloat())
      case DDouble => Some(in.readDouble())
      case DChar => Some(in.readChar())
      case DString => Some(nameOf(in.readUnsignedShort(), pool))
      case DEnum =>
        val name = nameOf(in.readUnsignedShort(), pool)
        enumValueOf(paramClazz, name)
      case other => throw new IllegalArgumentException(s"Unknown default tag $other")
  }

  /** Resolves a pool index: used builtin, reserved (unused) builtin zone, or explicit pool entry. */
  private def nameOf(idx: Int, pool: Array[String]): String =
    if idx < Builtins.size then Builtins.names(idx)
    else if idx < BuiltinBoundary then throw new IllegalArgumentException(s"Unreserved builtin pool index $idx")
    else pool(idx - BuiltinBoundary)

  private def typeName(clazz: Class[_]): String = clazz.getName.replace('.', '/')

  /** Restores a Class from a JVM internal name ('/' separators) or a primitive name. */
  private def classFor(name: String): Class[_] = {
    val jvm = name.replace('/', '.')
    jvm match
      case "int" => java.lang.Integer.TYPE
      case "long" => java.lang.Long.TYPE
      case "short" => java.lang.Short.TYPE
      case "boolean" => java.lang.Boolean.TYPE
      case "byte" => java.lang.Byte.TYPE
      case "char" => java.lang.Character.TYPE
      case "float" => java.lang.Float.TYPE
      case "double" => java.lang.Double.TYPE
      case "void" => java.lang.Void.TYPE
      case other =>
        val loader = Option(Thread.currentThread.getContextClassLoader).getOrElse(getClass.getClassLoader)
        Class.forName(other, false, loader)
  }

  /** Resolves an enum constant by name. Returns None if clazz is not an enum or name is invalid. */
  private def enumValueOf(clazz: Class[_], name: String): Option[Any] = {
    if Enums.isEnum(clazz) then {
      try
        val m = clazz.getMethod("valueOf", classOf[String])
        Some(m.invoke(null, name))
      catch
        case _: Exception => None
    } else None
  }

  private final class StringPool {
    // explicit entries only; their indices are BuiltinBoundary + position (builtin zone is fixed, zero bytes)
    private val map = mutable.LinkedHashMap.empty[String, Int]

    /** Index for a type name: fixed builtin index if known, else an explicit pool entry. */
    def typeIndex(name: String): Int = Builtins.indexOf(name).getOrElse(index(name))

    /** Index for an arbitrary string (property/method names, defaults): explicit pool entry only. */
    def index(s: String): Int = {
      map.getOrElseUpdate(s, {
        val idx = BuiltinBoundary + map.size
        if idx >= MaxPool then throw new IllegalArgumentException(s"Beaninfo string pool overflow: $s")
        idx
      })
    }

    def size: Int = map.size

    def strings: Iterable[String] = map.keys
  }
}
