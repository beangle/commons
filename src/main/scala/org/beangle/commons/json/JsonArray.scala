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
import org.beangle.commons.lang.{Numbers, Options}

import java.util as ju
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

  /** Parses index from path part (e.g. "0", "-1", "[1]", or "[-2]").
   *
   * @param part the path part
   * @return Some(index) if valid, None otherwise
   */
  protected[json] def parseIndex(part: String): Option[Int] = {
    if part.isEmpty then None
    else if (part.charAt(0) == '[' && part.charAt(part.length - 1) == ']') {
      val idxStr = part.substring(1, part.length - 1)
      if (Numbers.isDigits(idxStr) || (idxStr.startsWith("-") && Numbers.isDigits(idxStr.substring(1)))) Some(idxStr.toInt)
      else None
    } else if (Numbers.isDigits(part) || (part.startsWith("-") && Numbers.isDigits(part.substring(1)))) {
      Some(part.toInt)
    } else {
      None
    }
  }
}

/** Represents a JSON array.
 */
final class JsonArray extends collection.Seq[Any], Json {

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
    parseIndex(property) match
      case Some(i) => get(i)
      case None => None
  }

  /** Gets value at the given index.
   *
   * @param i the index
   * @return Some(value) or None if out of bounds
   */
  def get(i: Int): Option[Any] = {
    val index = if i >= 0 then i else values.size + i
    if (index >= 0 && index < values.size) {
      Some(values(index))
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
      o match {
        case jo: JsonObject =>
          o = jo.get(part).orNull
        case ja: JsonArray =>
          if ("*" == part) {
            o = flattenIfNested(ja)
          } else {
            val index = parseIndex(part)
            if (index.nonEmpty) {
              o = ja.get(index.get).orNull
            } else if (i > 0 && paths(i - 1) == "*") {
              o = new JsonArray(ja.values.map {
                case j: JsonObject => j.get(part).orNull
                case _ => null
              }.filter(_ != null))
            } else {
              o = null
            }
          }
      }
      i += 1
    }
    Option(o)
  }

  /** Flattens one array level for wildcard projection.
   *
   * Used by path part `*`: if current array contains nested arrays/lists,
   * flatten one level so patterns like `matrix[*][*]` can keep projecting.
   */
  private def flattenIfNested(array: JsonArray): JsonArray = {
    val flattened = mutable.ArrayBuffer[Any]()
    var hasNested = false
    array.values.foreach {
      case a: JsonArray =>
        hasNested = true
        flattened.addAll(a.values)
      case l: ju.List[?] =>
        hasNested = true
        val it = l.iterator()
        while (it.hasNext) {
          flattened += it.next()
        }
      case s: collection.Seq[?] =>
        hasNested = true
        flattened.addAll(s.asInstanceOf[collection.Seq[Any]])
      case v =>
        flattened += v
    }
    if hasNested then new JsonArray(flattened) else array
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
    get(Json.resolvePath(path))
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

  override def equals(o: Any): Boolean = {
    o match {
      case that: JsonArray => that.values.equals(this.values)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    this.values.hashCode()
  }

  override def toString: String = toJson
}
