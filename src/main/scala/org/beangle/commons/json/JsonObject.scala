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

import org.beangle.commons.conversion.string.TemporalConverter
import org.beangle.commons.lang.Strings

import java.time.{Instant, LocalDate, LocalDateTime}
import scala.collection.mutable

/** JSON object utilities.
 */
object JsonObject {

  def apply(v: (String, Any)*): JsonObject = {
    new JsonObject(v)
  }

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
  private var props: Map[String, Any] = Map()

  def this(v: Iterable[(String, Any)]) = {
    this()
    v foreach { x => add(x._1, x._2) }
  }

  def keys: Iterable[String] = props.keys

  def values: Map[String, Any] = props

  def query(path: String): Option[Any] = {
    val parts = if (path.charAt(0) == '/') Strings.split(path, "/") else Strings.split(path, ".")
    var i = 0
    var o: Any = this
    while (o != null && i < parts.length) {
      val part = parts(i)
      i += 1
      o match
        case null => o = null
        case jo: JsonObject => o = jo.props.getOrElse(part, null)
        case ja: JsonArray => o = ja.get(Array(part))
        case _ => o = null
    }
    Option(o)
  }

  def remove(key: String): JsonObject = {
    props -= key
    this
  }

  def add(key: String, value: Any): JsonObject = {
    if (value == null) {
      props -= key
    } else {
      props += key -> value
    }
    this
  }

  def addAll(datas: collection.Map[String, Any]): JsonObject = {
    datas foreach { case (k, v) =>
      props += k -> v
    }
    this
  }

  def addAll(datas: JsonObject): JsonObject = {
    datas.props foreach { case (k, v) =>
      this.props += k -> v
    }
    this
  }

  def apply(key: String): Any = {
    props(key)
  }

  def get(key: String): Option[Any] = {
    props.get(key)
  }

  def getString(key: String, defaultValue: String = ""): String = {
    props.get(key) match {
      case Some(s) => s.toString
      case _ => defaultValue
    }
  }

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

  def getDouble(key: String, defaultValue: Double = 0d): Double = {
    props.get(key) match {
      case Some(s) =>
        s match
          case n: Number => n.doubleValue()
          case s => s.toString.toDouble
      case _ => defaultValue
    }
  }

  def getDate(key: String, defaultValue: LocalDate = null): LocalDate = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[LocalDate])
      case _ => defaultValue
    }
  }

  def getDateTime(key: String, defaultValue: LocalDateTime = null): LocalDateTime = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[LocalDateTime])
      case _ => defaultValue
    }
  }

  def getInstant(key: String, defaultValue: Instant = null): Instant = {
    props.get(key) match {
      case Some(s) => TemporalConverter.convert(s.toString, classOf[Instant])
      case _ => defaultValue
    }
  }

  def getObject(key: String, defaultValue: JsonObject = null): JsonObject = {
    props.get(key) match {
      case Some(s) => s.asInstanceOf[JsonObject]
      case _ => if defaultValue == null then new JsonObject() else defaultValue
    }
  }

  def getArray(key: String): JsonArray = {
    props.get(key) match {
      case Some(s) => s.asInstanceOf[JsonArray]
      case _ => new JsonArray
    }
  }

  def contains(key: String): Boolean = {
    props.contains(key)
  }

  def toJson: String = {
    val sb = new StringBuilder("{")
    props.foreach(kv => {
      kv._2 match {
        case o: JsonObject => sb.append(kv._1).append(":").append(o.toJson)
        case a: JsonArray => sb.append(kv._1).append(":").append(a.toJson)
        case _ => sb.append(kv._1).append(":").append(JsonObject.toLiteral(kv._2))
      }
      sb.append(",")
    })
    if (props.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("}").toString()
  }

  override def iterator: Iterator[(String, Any)] = props.iterator

  override def toString: String = toJson
}
