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

import org.beangle.commons.lang.{Numbers, Strings}

import scala.collection.mutable

object JsonArray {

  def apply(v: Any*): JsonArray = {
    new JsonArray(v)
  }
}

/** Represents a JSON array.
 */
class JsonArray extends collection.Seq[Any] {

  def this(v: Iterable[Any]) = {
    this()
    values.addAll(v)
  }

  private val values = new mutable.ArrayBuffer[Any]

  def add(value: Any): Unit = {
    values.addOne(value)
  }

  def get(i: Int): Option[Any] = {
    if (i >= 0 && i < values.size) {
      Some(values(i))
    } else {
      None
    }
  }

  def get(paths: Array[String]): Any = {
    var i = 0
    var o: Any = this
    while (o != null && i < paths.length) {
      val part = paths(i)
      i += 1
      o match
        case jo: JsonObject => o = jo.get(part).orNull
        case ja: JsonArray =>
          var index = -1
          if (part.startsWith("[")) {
            index = part.substring(1, part.length - 1).toInt
          } else if (Numbers.isDigits(part)) {
            index = part.toInt
          }
          if (index > -1) {
            o = ja.get(index).orNull
          } else {
            o = new JsonArray(ja.values.map {
              case j: JsonObject => j.get(part).orNull
              case a: JsonArray => null
              case _ => null
            }.filter(_ != null))
          }
    }
    o
  }

  def query(path: String): Any = {
    val parts = if (path.charAt(0) == '/') Strings.split(path, "/") else Strings.split(path, ".")
    get(parts)
  }

  def toJson: String = {
    val sb = new StringBuilder("[")
    values.foreach(v => {
      v match {
        case o: JsonObject => sb.append(o.toJson)
        case a: JsonArray => sb.append(a.toJson)
        case _ => sb.append(JsonObject.toLiteral(v))
      }
      sb.append(",")
    })
    if (values.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("]").toString()
  }

  override def iterator: Iterator[Any] = values.iterator

  override def apply(i: Int): Any = {
    values(i)
  }

  override def length: Int = values.length

  override def toString: String = toJson
}
