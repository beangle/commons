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
      case None => "null"
      case Some(iv) => valueToLiteral(iv)
      case _ => valueToLiteral(v)
    }
  }

  private def valueToLiteral(v: Any): String = {
    v match {
      case s: String => escape(s)
      case b: Boolean => b.toString
      case s: Short => s.toString
      case n: Int => n.toString
      case f: Float => f.toString
      case d: Double => d.toString
      case l: Long => escape(l.toString)
      case _ => escape(v.toString)
    }
  }

  private def escape(s: String): String = {
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
class JsonObject extends DynamicBean, Json {
  private val props: mutable.Map[String, Any] = Collections.newMap[String, Any]

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
  /** 根据路径更新或生成对象
   *
   * @param path
   * @param value
   * @return
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

  /** 将查询路径转换成属性数组
   *  /a/b/3/c 转换成[a,b,3,c]
   *  a.b[3].c 转换成[a,b,[3],c]
   * @param path
   * @return
   */
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

  /** 删除keys
   *
   * @param keys
   * @return
   */
  def remove(keys: String*): JsonObject = {
    keys foreach { key =>
      props -= key
    }
    this
  }

  override def -(key: String): collection.Map[String, Any] = {
    props -= key
    this
  }

  override def -(key1: String, key2: String, keys: String*): collection.Map[String, Any] = {
    props -= key1
    props -= key2
    keys foreach { key =>
      props -= key
    }
    this
  }

  /** 添加直接属性
   *
   * @param key
   * @param value
   * @return
   */
  def add(key: String, value: Any): JsonObject = {
    if (value == null) {
      props -= key
    } else {
      props += key -> value
    }
    this
  }

  /** 批量添加属性
   *
   * @param datas
   * @return
   */
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

  override def apply(key: String): Any = {
    props(key)
  }

  override def get(key: String): Option[Any] = {
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

  override def toJson: String = {
    val sb = new StringBuilder("{")
    props.foreach { kv =>
      sb.append("\"").append(kv._1).append("\":")
      kv._2 match {
        case o: JsonObject => sb.append(o.toJson)
        case a: JsonArray => sb.append(a.toJson)
        case _ => sb.append(JsonObject.toLiteral(kv._2))
      }
      sb.append(",")
    }
    if (props.nonEmpty) sb.deleteCharAt(sb.length - 1)
    sb.append("}").toString()
  }

  override def iterator: Iterator[(String, Any)] = props.iterator

  /**
   * 检查当前JSON对象是否与目标JSON对象匹配
   * 此方法用于深度比较两个JSON对象的结构和内容，以确定它们是否在结构上相等
   *
   * @param target 目标JSON对象，用于与当前对象进行比较
   * @return 如果当前JSON对象与目标JSON对象匹配，则返回true；否则返回false
   */
  def isMatch(target: JsonObject): Boolean = {
    // 遍历目标JSON对象的所有键，检查每个键值对是否与当前对象中的键值对匹配
    target.keys.forall { k =>
      // 获取目标JSON对象中键k对应的值
      val t = target(k)
      // 如果当前对象包含键k，则进行匹配检查
      if this.contains(k) then
        // 根据值的类型，进行不同的匹配逻辑
        t match {
          // 如果值是JsonObject类型，则递归调用isMatch方法进行深度匹配
          case jo: JsonObject =>
            this (k) match {
              case sjo: JsonObject => sjo.isMatch(jo)
              case _ => false
            }
          // 如果值是JsonArray类型，则调用isMatch方法比较数组内容
          case ja: JsonArray =>
            this (k) match {
              case sja: JsonArray => isMatch(sja, ja)
              case sjo: JsonObject => false
              case v: Any => ja.contains(v)
            }
          // 如果值是其他类型，则直接比较值是否相等
          case v: Any => this (k) == t
        }
      // 如果当前对象不包含键k，则返回false，表示不匹配
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
