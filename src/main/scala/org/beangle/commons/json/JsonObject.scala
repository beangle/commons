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

import org.beangle.commons.lang.Strings

import scala.collection.mutable

/** JSON object utilities.
 */
object JsonObject {

  def toLiteral(v: Any): String = {
    v match {
      case null => "null"
      case Null => "null"
      case s: String => toString(s)
      case _ => toString(v.toString)
    }
  }

  def toString(s: String): String = {
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

/** Represents a JSON object.
 */
class JsonObject extends Iterable[(String, Any)] {
  private var map: Map[String, Any] = Map()

  def query(path: String): Option[Any] = {
    val parts = if (path.charAt(0) == '/') Strings.split(path, "/") else Strings.split(path, ".")
    var i = 0
    var o: Any = this
    while (o != null && i < parts.length) {
      val part = parts(i)
      i += 1
      o match
        case null => o = null
        case jo: JsonObject => o = jo.map.getOrElse(part, null)
        case ja: JsonArray => o = ja.get(Array(part))
        case _ => o = null
    }
    Option(o)
  }

  def add(key: String, value: Any): Unit = {
    if (value == null) {
      map -= key
    } else {
      map += key -> value
    }
  }

  def get(key: String): Option[Any] = {
    map.get(key)
  }

  def getString(key: String, defaultValue: String = ""): String = {
    map.get(key) match {
      case Some(s) => s.toString
      case _ => defaultValue
    }
  }

  def contains(key: String): Boolean = {
    map.contains(key)
  }

  def toJson: String = {
    val sb = new StringBuilder("{")
    map.foreach(kv => {
      kv._2 match {
        case o: JsonObject => sb.append(kv._1).append(":").append(o.toJson)
        case a: JsonArray => sb.append(kv._1).append(":").append(a.toJson)
        case _ => sb.append(kv._1).append(":").append(JsonObject.toLiteral(kv._2))
      }
      sb.append(",")
    })
    if (map.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("}").toString()
  }

  override def iterator: Iterator[(String, Any)] = map.iterator

  override def toString: String = toJson
}


