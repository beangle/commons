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

package org.beangle.commons.collection

import java.time.{ LocalDate, LocalDateTime }

import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.Strings.{ isNotEmpty, split }
import org.beangle.commons.lang.{ Objects, Strings }

import scala.collection.Map
import scala.reflect.ClassTag

/**
 * MapConverter class.
 *
 * @author chaostone
 */
class MapConverter(val conversion: DefaultConversion = DefaultConversion.Instance) {
  /**
   * convert value to target class or array[class]
   */
  def convert[T](value: Any, clazz: Class[T]): Option[T] = {
    if (null == value) return None
    if (clazz.isAssignableFrom(value.getClass)) return Some(value.asInstanceOf[T])
    value match {
      case s: String => if Strings.isEmpty(s) then None else Some(conversion.convert(s, clazz))
      case a: Array[_] => if !clazz.isArray then (if a.length >0 then convert(a(0), clazz) else None) else Some(conversion.convert(value, clazz))
      case i: Iterable[_] => if !clazz.isArray then convert(i.head, clazz) else Some(conversion.convert(value.asInstanceOf[Iterable[_]].toArray, clazz))
      case o: Any => Some(conversion.convert(value, clazz))
    }
  }

  /**
   * convert.
   */
  def convert[T](datas: Array[_], clazz: Class[T]): Array[T] = {
    if (null == datas) return null
    val newDatas = java.lang.reflect.Array.newInstance(clazz, datas.size).asInstanceOf[Array[T]]
    for (i <- 0 until datas.length) newDatas(i) = convert(datas(i), clazz).getOrElse(Objects.default(clazz))
    newDatas
  }

  /**
   * get.
   */
  def get[T](data: Map[String, Any], name: String, clazz: Class[T]): Option[T] =
    data.get(name) match {
      case Some(value) => convert(value, clazz)
      case _ => None
    }

  def getBoolean(data: Map[String, Any], name: String): Option[Boolean] =
    get(data, name, classOf[Boolean])

  def getDate(data: Map[String, Any], name: String): Option[LocalDate] =
    get(data, name, classOf[LocalDate])

  def getDateTime(data: Map[String, Any], name: String): Option[LocalDateTime] =
    get(data, name, classOf[LocalDateTime])

  def getFloat(data: Map[String, Any], name: String): Option[Float] =
    get(data, name, classOf[Float])

  def getInt(data: Map[String, Any], name: String): Option[Int] =
    get(data, name, classOf[Int])

  def getShort(data: Map[String, Any], name: String): Option[Short] =
    get(data, name, classOf[Short])

  def getLong(data: Map[String, Any], name: String): Option[Long] =
    get(data, name, classOf[Long])

  /**
   * 返回data中以prefix.开头的参数
   *
   * @param exclusiveAttrNames 要排除的属性串
   */
  def sub(data: Map[String, Any], prefix: String, exclusiveAttrNames: String): Map[String, Any] =
    sub(data, prefix, exclusiveAttrNames, true)

  /**
   * submap
   */
  def sub(data: Map[String, Any], prefix: String): Map[String, Any] = sub(data, prefix, null, true)

  /**
   * sub map
   */
  def sub(data: Map[String, Any], prefix: String, exclusiveAttrNames: String, stripPrefix: Boolean): Map[String, Any] = {
    val excludes: Set[String] = if (isNotEmpty(exclusiveAttrNames)) split(exclusiveAttrNames, ",").toSet else Set.empty
    val newParams = new collection.mutable.HashMap[String, Any]
    for ((key, value) <- data)
      if ((key.indexOf(prefix + ".") == 0) && (!excludes.contains(key)))
        newParams.put((if (stripPrefix) key.substring(prefix.length + 1) else key), value)
    newParams.toMap
  }
}
