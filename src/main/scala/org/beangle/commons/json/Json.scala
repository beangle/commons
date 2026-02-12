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

import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.lang.{Options, Strings}

/** JSON parse, query, and serialization. */
object Json {

  /** Parses a JSON string into a Json value (object or array). Returns JsonValue("") for blank input.
   *
   * @param s the JSON string to parse
   * @return the parsed Json value
   */
  def parse(s: String): Json = {
    if Strings.isBlank(s) then JsonValue("")
    else new JsonParser(new java.io.StringReader(s)).parse()
  }

  /** Queries the JSON at the given path. Path uses slash or dot for nested properties.
   *
   * @param s    the JSON string to query
   * @param path the query path (e.g. [index]/property or property.nested)
   * @return the value at the path, or None if not found
   */
  def query(s: String, path: String): Option[Any] = {
    parse(s).query(path)
  }

  /** Parses a JSON string as a JsonObject. Returns empty object for blank input.
   *
   * @param s the JSON string
   * @return the JsonObject
   */
  def parseObject(s: String): JsonObject = {
    if Strings.isBlank(s) then new JsonObject() else parse(s).asInstanceOf[JsonObject]
  }

  /** Parses a JSON string as a JsonArray. Returns empty array for blank input.
   *
   * @param s the JSON string
   * @return the JsonArray
   */
  def parseArray(s: String): JsonArray = {
    if Strings.isBlank(s) then new JsonArray() else parse(s).asInstanceOf[JsonArray]
  }

  /** Converts a Map to its JSON string representation.
   *
   * @param datas the map to convert
   * @return the JSON string
   */
  def toJson(datas: collection.Map[_, _]): String = {
    if (datas.isEmpty) return "{}"
    val sb = new StringBuilder("{")
    val kvs = datas.filter(_._1.isInstanceOf[String])
    kvs.foreach { kv =>
      sb.append("\"").append(kv._1.toString).append("\":")
      Options.unwrap(kv._2) match {
        case map: collection.Map[_, _] => sb.append(toJson(map))
        case seq: collection.Seq[_] => sb.append(toJson(seq))
        case v => sb.append(toLiteral(v))
      }
      sb.append(",")
    }
    if (kvs.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("}").toString
  }

  /** Converts a JsonObject to its JSON string.
   *
   * @param value the JsonObject
   * @return the JSON string
   */
  def toJson(value: JsonObject): String = {
    value.toJson
  }

  /** Converts a JsonArray to its JSON string.
   *
   * @param value the JsonArray
   * @return the JSON string
   */
  def toJson(value: JsonArray): String = {
    value.toJson
  }

  /** Converts an Iterable to its JSON array string representation.
   *
   * @param data the iterable to convert
   * @return the JSON array string
   */
  def toJson(data: Iterable[_]): String = {
    val sb = new StringBuilder("[")
    for (li <- data) {
      Options.unwrap(li) match {
        case map: collection.Map[_, _] => sb.append(toJson(map))
        case seq: collection.Seq[_] => sb.append(toJson(seq))
        case _ => sb.append(toLiteral(li))
      }
      sb.append(",")
    }
    if (data.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("]").toString
  }

  /** Returns a new empty JsonObject.
   *
   * @return empty JsonObject
   */
  def emptyObject: JsonObject = new JsonObject()

  /** Returns a new empty JsonArray.
   *
   * @return empty JsonArray
   */
  def emptyArray: JsonArray = new JsonArray()

  /** Converts a value to its JSON literal representation.
   *
   * @param v the value
   * @return the literal string
   */
  def toLiteral(v: Any): String = {
    v match {
      case null => "null"
      case Null => "null"
      case None => "null"
      case Some(iv) => valueToLiteral(iv)
      case _ => valueToLiteral(v)
    }
  }

  private def valueToLiteral(v: Any): String = {
    v match {
      case s: String => Json.escape(s)
      case b: Boolean => b.toString
      case s: Short => s.toString
      case n: Int => n.toString
      case f: Float => f.toString
      case d: Double => d.toString
      case l: Long => Json.escape(l.toString)
      case bs: Array[Byte] => s""""${Base64.encode(bs)}""""
      case _ => Json.escape(v.toString)
    }
  }

  /** Escapes special characters in a string for JSON representation.
   *
   * @param s the string to escape
   * @return the escaped string with quotes
   */
  def escape(s: String): String = {
    val length = s.length
    val text = s.toCharArray
    val sb = new StringBuilder()
    sb.append('\"')
    (0 until length) foreach { i =>
      val c = text(i)
      c match {
        case '"' => sb.append("\\\"")
        case '\\' => sb.append("\\\\")
        case '\b' => sb.append("\\b")
        case '\f' => sb.append("\\f")
        case '\n' => sb.append("\\n")
        case '\r' => sb.append("\\r")
        case '\t' => sb.append("\\t")
        case _ =>
          if (c > 0x1f) {
            sb.append(c)
          } else {
            sb.append("\\u")
            val hex = "000" + Integer.toHexString(c)
            sb.append(hex.substring(hex.length() - 4))
          }
      }
    }
    sb.append('\"')
    sb.toString()
  }
}

/** JSON tree node (object, array, or leaf value). */
trait Json {

  /** Queries the object graph using slash or dot to separate nested properties.
   * Path format: [index]/property_name or property.nested.property
   *
   * @param path the query path
   * @return the value at the path, or None if not found
   */
  def query(path: String): Option[Any]

  /** Gets the value of the specified property.
   *
   * @param property the property name
   * @return the property value, or None if not found
   */
  def get(property: String): Option[Any]

  /** Returns the child nodes of this Json node. */
  def children: Iterable[Json]

  /** Navigates to the specified property. Returns empty object if not found.
   *
   * @param property the property name to navigate to
   * @return the Json at the property, or empty object if not found
   */
  def \(property: String): Json = {
    get(property) match {
      case None => Json.emptyObject
      case Some(o) =>
        o match {
          case jo: JsonObject => jo
          case ja: JsonArray => ja
          case _ => JsonValue(o)
        }
    }
  }

  /** Serializes this node to JSON string. */
  def toJson: String

  /** Returns the raw value of this node. */
  def value: Any
}
