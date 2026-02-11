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

object Json {

  def parse(s: String): Json = {
    if Strings.isBlank(s) then JsonValue("")
    else new JsonParser(new java.io.StringReader(s)).parse()
  }

  def query(s: String, path: String): Option[Any] = {
    parse(s).query(path)
  }

  def parseObject(s: String): JsonObject = {
    if Strings.isBlank(s) then new JsonObject() else parse(s).asInstanceOf[JsonObject]
  }

  def parseArray(s: String): JsonArray = {
    if Strings.isBlank(s) then new JsonArray() else parse(s).asInstanceOf[JsonArray]
  }

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

  def toJson(value: JsonObject): String = {
    value.toJson
  }

  def toJson(value: JsonArray): String = {
    value.toJson
  }

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

  def emptyObject: JsonObject = {
    new JsonObject()
  }

  def emptyArray: JsonArray = {
    new JsonArray()
  }

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

trait Json {
  /** Query object graph,using slash or dot seperate nested property,like
   * [index]/property_name/property_name or [index].property_name.property_name
   *
   * @param path query path
   * @return
   */
  def query(path: String): Option[Any]

  /** Get Property
   *
   * @param property name
   * @return
   */
  def get(property: String): Option[Any]

  /** 该节点的下级节点
   *
   * @return
   */
  def children: Iterable[Json]

  /** Navigate property
   *
   * @param path property
   * @return
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

  def toJson: String

  def value: Any
}
