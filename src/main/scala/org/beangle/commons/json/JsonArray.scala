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

import org.beangle.commons.json.JsonArray.parseIndex
import org.beangle.commons.lang.{Numbers, Options, Strings}

import scala.collection.mutable

/** JsonArray factory. */
object JsonArray {

  /** Creates JsonArray from values.
   *
   * @param v the values
   * @return the new JsonArray
   */
  def apply(v: Any*): JsonArray = {
    new JsonArray(v)
  }

  /** Parses index from path part (e.g. "0" or "[1]").
   *
   * @param part the path part
   * @return the index, or -1 if not an index
   */
  protected[json] def parseIndex(part: String): Int = {
    var index = -1
    if (part.charAt(0) == '[') {
      index = part.substring(1, part.length - 1).toInt
    } else if (Numbers.isDigits(part)) {
      index = part.toInt
    }
    index
  }
}

/** Represents a JSON array.
 */
class JsonArray extends collection.Seq[Any], Json {

  private val values = new mutable.ArrayBuffer[Any]

  /** Creates JsonArray from iterable.
   *
   * @param v the values
   */
  def this(v: Iterable[Any]) = {
    this()
    values.addAll(v)
  }

  /** Appends a value to the array.
   *
   * @param value the value to add
   */
  def add(value: Any): Unit = {
    values.addOne(value)
  }

  /** Gets value by property (index as string).
   *
   * @param property the index string (e.g. "0" or "[1]")
   * @return Some(value) or None
   */
  override def get(property: String): Option[Any] = {
    get(parseIndex(property))
  }

  /** Gets value at the given index.
   *
   * @param i the index
   * @return Some(value) or None if out of bounds
   */
  def get(i: Int): Option[Any] = {
    if (i >= 0 && i < values.size) {
      Some(values(i))
    } else {
      None
    }
  }

  /** Removes the first occurrence of the value.
   *
   * @param value the value to remove
   * @return this for chaining
   */
  def substractOne(value: Any): this.type = {
    values.subtractOne(value)
    this
  }

  /** Gets value at the nested path (supports object keys and array indices).
   *
   * @param paths the path parts (e.g. ["a", "0", "b"])
   * @return Some(value) or None
   */
  def get(paths: Array[String]): Option[Any] = {
    var i = 0
    var o: Any = this
    while (o != null && i < paths.length) {
      val part = paths(i)
      i += 1
      o match
        case jo: JsonObject => o = jo.get(part).orNull
        case ja: JsonArray =>
          val index = parseIndex(part)
          if (index > -1) {
            o = if index >= ja.length then null else ja.get(index).orNull
          } else {
            o = new JsonArray(ja.values.map {
              case j: JsonObject => j.get(part).orNull
              case a: JsonArray => null
              case _ => null
            }.filter(_ != null))
          }
    }
    Option(o)
  }

  /** Auto-expands the array and sets the element at the given index.
   *
   * @param i     the index
   * @param value the value to set
   */
  def set(i: Int, value: Any): Unit = {
    require(i >= 0)
    if (i < values.size) {
      values(i) = value
    } else {
      for (j <- values.size until i + 1) {
        values.addOne(null)
      }
      values(i) = value
    }
  }

  override def query(path: String): Option[Any] = {
    val parts = if (path.charAt(0) == '/') Strings.split(path, "/") else Strings.split(path, ".")
    get(parts)
  }

  override def toJson: String = {
    val sb = new StringBuilder("[")
    values.foreach(v => {
      Options.unwrap(v) match {
        case o: JsonObject => sb.append(o.toJson)
        case a: JsonArray => sb.append(a.toJson)
        case v1 => sb.append(Json.toLiteral(v1))
      }
      sb.append(",")
    })
    if (values.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("]").toString()
  }

  override def iterator: Iterator[Any] = values.iterator

  /** Returns element at index.
   *
   * @param i the index
   * @return the element
   */
  override def apply(i: Int): Any = {
    values(i)
  }

  override def children: Iterable[Json] = {
    values.map {
      case jo: JsonObject => jo
      case ja: JsonArray => ja
      case v => JsonValue(v)
    }
  }

  override def length: Int = values.length

  override def value: Any = this

  override def toString: String = toJson
}
