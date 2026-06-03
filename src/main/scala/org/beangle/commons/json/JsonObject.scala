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
import org.beangle.commons.lang.Options

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

}

/** Represents a JSON object.
 */
final class JsonObject extends DynamicBean, Json {
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
    val parts = Json.resolvePath(path)
    var i = 0
    var o: Any = this
    while (o != null && i < parts.length) {
      val part = parts(i)
      i += 1
      o = o match
        case jo: JsonObject => jo.props.getOrElse(part, null)
        case ja: JsonArray =>
          val rest = parts.slice(i - 1, parts.length)
          i = parts.length
          ja.get(rest).orNull
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
   * Supports wildcard batch update on arrays:
   * - roles[*].name = "guest"
   * - matrix[*][*] = 0
   *
   * Update is strict: if path cannot be resolved or created under current rules,
   * this method throws IllegalArgumentException instead of silently ignoring it.
   *
   * @param path  the path to the property
   * @param value the value to set
   * @return this JsonObject for chaining
   */
  def update(path: String, value: Any): JsonObject = {
    val parts = Json.resolvePath(path)
    val cv = convert(value)
    if parts.nonEmpty && !updateAt(this, parts, 0, cv) then
      throw new IllegalArgumentException(s"Cannot update path: ${path}")
    this
  }

  private def updateAt(node: Any, parts: Array[String], idx: Int, value: Any): Boolean = {
    node match
      case jo: JsonObject => updateObjectNode(jo, parts, idx, value)
      case ja: JsonArray => updateArrayNode(ja, parts, idx, value)
      case _ => false
  }

  private def updateObjectNode(jo: JsonObject, parts: Array[String], idx: Int, value: Any): Boolean = {
    val part = parts(idx)
    val isLast = idx == parts.length - 1
    // Object node only accepts property names; wildcard/index token is array-only.
    if part == "*" || JsonArray.parseIndex(part).nonEmpty then false
    else if isLast then
      jo.add(part, value)
      true
    else
      val nextPart = parts(idx + 1)
      val child = jo.props.getOrElseUpdate(part, if isArrayToken(nextPart) then new JsonArray else new JsonObject)
      updateAt(child, parts, idx + 1, value)
  }

  /** Handles both index update and wildcard batch update on array node. */
  private def updateArrayNode(ja: JsonArray, parts: Array[String], idx: Int, value: Any): Boolean = {
    val part = parts(idx)
    val isLast = idx == parts.length - 1
    if part == "*" then
      // Wildcard means update all elements at this level.
      if isLast then
        var i = 0
        while (i < ja.length) {
          ja.set(i, value)
          i += 1
        }
        true
      else
        var i = 0
        var updated = false
        while (i < ja.length) {
          if updateAt(ja(i), parts, idx + 1, value) then updated = true
          i += 1
        }
        updated
    else
      JsonArray.parseIndex(part) match
        case Some(rawIndex) =>
          normalizeIndex(ja.length, rawIndex) match
            case Some(targetIndex) =>
              if isLast then
                ja.set(targetIndex, value)
                true
              else
                val nextPart = parts(idx + 1)
                val child = ja.get(rawIndex) match
                  case Some(v) => v
                  case None =>
                    // Negative index never auto-creates new slots.
                    if rawIndex < 0 then return false
                    val created = if isArrayToken(nextPart) then new JsonArray else new JsonObject
                    ja.set(targetIndex, created)
                    created
                updateAt(child, parts, idx + 1, value)
            case None => false
        case None => false
  }

  private def isArrayToken(part: String): Boolean = {
    part == "*" || JsonArray.parseIndex(part).nonEmpty
  }

  private def normalizeIndex(length: Int, index: Int): Option[Int] = {
    val target = if index >= 0 then index else length + index
    if target >= 0 then Some(target) else None
  }

  private def convert(value: Any): Any = {
    Json.of(value).value
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
      val jv =
        value match {
          case jo: Json => jo
          case i: Iterable[_] =>
            if (i.isEmpty) Json.emptyArray
            else {
              val isTuples = i.forall(x => x.isInstanceOf[(_, _)])
              if (isTuples) {
                new JsonObject(i.map { x => val t = x.asInstanceOf[(_, _)]; (t._1.toString, t._2) })
              } else {
                new JsonArray(i)
              }
            }
          case _ => value
        }
      props += key -> jv
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
      add(k, v)
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
    val d = props(key)
    if (d == Null) null else d
  }

  override def get(key: String): Option[Any] = {
    props.get(key) match {
      case v@Some(a) => if (a == Null) None else v
      case None => None
    }
  }

  /** Gets string value, or default if missing.
   *
   * @param key          the property name
   * @param defaultValue the default when not found
   * @return the string value
   */
  def getString(key: String, defaultValue: String = ""): String = {
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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
    get(key) match {
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

  override def equals(o: Any): Boolean = {
    o match {
      case that: JsonObject => that.props.equals(this.props)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    this.props.hashCode()
  }

  override def value: Any = this

  override def toString: String = toJson
}
