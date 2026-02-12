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

package org.beangle.commons.json

import org.beangle.commons.bean.DynamicBean
import org.beangle.commons.collection.Collections
import org.beangle.commons.conversion.string.TemporalConverter
import org.beangle.commons.lang.{Options, Strings}

import java.time.{Instant, LocalDate, LocalDateTime}
import scala.collection.mutable

/** JSON object utilities.
 */
object JsonObject {

  /** Creates JsonObject from key-value pairs.
   *
   * @param v the key-value pairs
   * @return the new JsonObject
   */
  def apply(v: (String, Any)*): JsonObject = {
    new JsonObject(v)
  }

  /** Converts value to JSON literal. Deprecated: use Json.toLiteral.
   *
   * @param v the value
   * @return the literal string
   */
  @deprecated("Using Json.toLiteral", "5.7.1")
  def toLiteral(v: Any): String = {
    Json.toLiteral(v)
  }
}

/** Represents a JSON object.
 */
class JsonObject extends DynamicBean, Json {
  private val props: mutable.Map[String, Any] = Collections.newMap[String, Any]

  /** Creates JsonObject from iterable of key-value pairs.
   *
   * @param v the key-value pairs
   */
  def this(v: Iterable[(String, Any)]) = {
    this()
    v foreach { x => add(x._1, x._2) }
  }

  override def query(path: String): Option[Any] = {
    val parts = splitPath(path)
    var i = 0
    var o: Any = this
    while (o != null && i < parts.length) {
      val part = parts(i)
      i += 1
      o = o match
        case jo: JsonObject => jo.props.getOrElse(part, null)
        case ja: JsonArray => ja.get(Array(part)).orNull
        case _ => null
    }
    Option(o)
  }

  override def children: Iterable[Json] = {
    props.values.map {
      case jo: JsonObject => jo
      case ja: JsonArray => ja
      case v => JsonValue(v)
    }
  }

  /** Updates or creates objects at the given path (e.g. /a/b/3/c or a.b[3].c).
   *
   * @param path  the path to the property
   * @param value the value to set
   * @return this JsonObject for chaining
   */
  def update(path: String, value: Any): JsonObject = {
    val parts = splitPath(path)
    var i = 0
    var o: Any = this
    while (o != null && i < parts.length - 1) {
      val part = parts(i)
      val nextIdx = JsonArray.parseIndex(parts(i + 1))
      i += 1
      o match
        case jo: JsonObject =>
          o = jo.props.getOrElseUpdate(part, if nextIdx > -1 then new JsonArray else new JsonObject)
        case ja: JsonArray =>
          val idx = JsonArray.parseIndex(part)
          ja.get(idx) match
            case None =>
              o = if nextIdx > -1 then new JsonArray else new JsonObject
              ja.set(idx, o)
            case Some(a) => o = a
        case _ => o = null
    }
    val cv = convert(value)
    val last = parts(i)
    o match
      case jo: JsonObject => jo.add(last, cv)
      case ja: JsonArray => ja.set(JsonArray.parseIndex(last), cv)

    this
  }

  /** Splits query path into property array. /a/b/3/c -> [a,b,3,c]; a.b[3].c -> [a,b,[3],c]. */
  private def splitPath(path: String): Array[String] = {
    if path.charAt(0) == '/' then
      Strings.split(path, "/")
    else {
      Strings.split(path, ".").flatMap { p =>
        val idx = p.indexOf('[')
        if (idx > 0 && p.charAt(p.length - 1) == ']') {
          Array(p.substring(0, idx), p.substring(idx))
        } else {
          Array(p)
        }
      }
    }
  }

  private def convert(value: Any): Any = {
    value match {
      case jo: JsonObject => jo
      case ja: JsonArray => ja
      case i: Iterable[Any] => new JsonArray(i)
      case a: Array[Any] => new JsonArray(a)
      case v: Any => v
    }
  }

  /** Removes the specified keys from this object.
   *
   * @param keys the property names to remove
   * @return this JsonObject for chaining
   */
  def remove(keys: String*): JsonObject = {
    keys foreach { key =>
      props -= key
    }
    this
  }

  /** Removes the key and returns this for chaining.
   *
   * @param key the key to remove
   * @return this
   */
  override def -(key: String): collection.Map[String, Any] = {
    props -= key
    this
  }

  /** Removes the given keys and returns this for chaining.
   *
   * @param key1 the first key
   * @param key2 the second key
   * @param keys additional keys
   * @return this
   */
  override def -(key1: String, key2: String, keys: String*): collection.Map[String, Any] = {
    props -= key1
    props -= key2
    keys foreach { key =>
      props -= key
    }
    this
  }

  /** Adds a direct property.
   *
   * @param key   the property name
   * @param value the property value (null removes the key)
   * @return this JsonObject for chaining
   */
  def add(key: String, value: Any): JsonObject = {
    if (value == null) {
      props -= key
    } else {
      props += key -> value
    }
    this
  }

  /** Adds all entries from the given map.
   *
   * @param datas the map of key-value pairs to add
   * @return this JsonObject for chaining
   */
  def addAll(datas: collection.Map[String, Any]): JsonObject = {
    datas foreach { case (k, v) =>
      props += k -> v
    }
    this
  }

  /** Adds all entries from the given JsonObject.
   *
   * @param datas the JsonObject to merge
   * @return this for chaining
   */
  def addAll(datas: JsonObject): JsonObject = {
    datas.props foreach { case (k, v) =>
      this.props += k -> v
    }
    this
  }

  /** Gets value by key. Throws if key not found.
   *
   * @param key the property name
   * @return the value
   */
  override def apply(key: String): Any = {
    props(key)
  }

  override def get(key: String): Option[Any] = {
    props.get(key)
  }

  /** Gets string value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the string value
   */
  def getString(key: String, defaultValue: String = ""): String = {
    props.get(key) match {
      case Some(s) => s.toString
      case _ => defaultValue
    }
  }

  /** Gets boolean value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the boolean value
   */
  def getBoolean(key: String, defaultValue: Boolean = false): Boolean = {
    props.get(key) match {
      case Some(s) =>
        s match
          case i: Boolean => i
          case n: Number => n.intValue() > 0
          case s => s.toString.toBoolean
      case _ => defaultValue
    }
  }

  /** Gets int value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the int value
   */
  def getInt(key: String, defaultValue: Int = 0): Int = {
    props.get(key) match {
      case Some(s) =>
        s match
          case i: Int => i
          case n: Number => n.intValue()
          case s => s.toString.toInt
      case _ => defaultValue
    }
  }

  /** Gets long value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the long value
   */
  def getLong(key: String, defaultValue: Long = 0l): Long = {
    props.get(key) match {
      case Some(s) =>
        s match
          case i: Long => i
          case n: Number => n.longValue()
          case s => s.toString.toLong
      case _ => defaultValue
    }
  }

  /** Gets double value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the double value
   */
  def getDouble(key: String, defaultValue: Double = 0d): Double = {
    props.get(key) match {
      case Some(s) =>
        s match
          case n: Number => n.doubleValue()
          case s => s.toString.toDouble
      case _ => defaultValue
    }
  }

  /** Gets LocalDate value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the LocalDate value
   */
  def getDate(key: String, defaultValue: LocalDate = null): LocalDate = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[LocalDate])
      case _ => defaultValue
    }
  }

  /** Gets LocalDateTime value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the LocalDateTime value
   */
  def getDateTime(key: String, defaultValue: LocalDateTime = null): LocalDateTime = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[LocalDateTime])
      case _ => defaultValue
    }
  }

  /** Gets Instant value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the Instant value
   */
  def getInstant(key: String, defaultValue: Instant = null): Instant = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[Instant])
      case _ => defaultValue
    }
  }

  /** Gets JsonObject value, or default/empty if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the JsonObject value
   */
  def getObject(key: String, defaultValue: JsonObject = null): JsonObject = {
    props.get(key) match {
      case Some(s) => s.asInstanceOf[JsonObject]
      case _ => if defaultValue == null then new JsonObject() else defaultValue
    }
  }

  /** Gets JsonArray value, or empty array if missing.
   *
   * @param key the property name
   * @return the JsonArray value
   */
  def getArray(key: String): JsonArray = {
    props.get(key) match {
      case Some(s) => s.asInstanceOf[JsonArray]
      case _ => new JsonArray
    }
  }

  override def toJson: String = {
    val sb = new StringBuilder("{")
    props.foreach { kv =>
      sb.append("\"").append(kv._1).append("\":")
      Options.unwrap(kv._2) match {
        case o: JsonObject => sb.append(o.toJson)
        case a: JsonArray => sb.append(a.toJson)
        case v => sb.append(Json.toLiteral(v))
      }
      sb.append(",")
    }
    if (props.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("}").toString()
  }

  override def iterator: Iterator[(String, Any)] = props.iterator

  /** Deep-compares this JSON object with the target for structural equality.
   *
   * @param target the JSON object to compare with
   * @return true if both objects match in structure and content
   */
  def isMatch(target: JsonObject): Boolean = {
    target.keys.forall { k =>
      val t = target(k)
      if this.contains(k) then
        t match {
          case jo: JsonObject =>
            this (k) match {
              case sjo: JsonObject => sjo.isMatch(jo)
              case _ => false
            }
          case ja: JsonArray =>
            this (k) match {
              case sja: JsonArray => isMatch(sja, ja)
              case sjo: JsonObject => false
              case v: Any => ja.contains(v)
            }
          case v: Any => this (k) == t
        }
      else false
    }
  }

  private def isMatch(src: JsonArray, target: JsonArray): Boolean = {
    if (src.size == target.size) {
      src.indices.forall { i =>
        val si = src(i)
        val ti = target(i)
        si match
          case jo: JsonObject if ti.isInstanceOf[JsonObject] => jo.isMatch(ti.asInstanceOf[JsonObject])
          case ja: JsonArray if ti.isInstanceOf[JsonArray] => isMatch(ja, ti.asInstanceOf[JsonArray])
          case _ => si == ti
      }
    } else false
  }

  override def value: Any = this

  override def toString: String = toJson
}
