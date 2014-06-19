/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.collection

import java.sql.Date
import java.util.Date

import scala.collection.Map
import scala.reflect.ClassTag

import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.{ Objects, Strings }
import org.beangle.commons.lang.Strings.{ isNotEmpty, split }
/**
 * MapConverter class.
 *
 * @author chaostone
 */
class MapConverter(val conversion: DefaultConversion = DefaultConversion.Instance) {
  /**
   * convert.
   */
  def convert[T](value: Any, clazz: Class[T]): T = {
    if (null == value) return Objects.default(clazz)
    if (value.isInstanceOf[String] && Strings.isEmpty(value.asInstanceOf[String])) return Objects.default(clazz)
    conversion.convert(value, clazz)
  }

  /**
   * convert.
   */
  def convert[T: ClassTag](datas: Array[AnyRef], clazz: Class[T]): Array[T] = {
    if (null == datas) return null
    val newDatas = new Array[T](datas.size);
    for (i <- 0 until datas.length) newDatas(i) = convert(datas(i), clazz)
    newDatas
  }

  /**
   * get.
   */
  def get[T](data: Map[String, Any], name: String, clazz: Class[T]): Option[T] = {
    data.get(name) match {
      case Some(value) =>
        if (null == value) None
        else {
          val rs = convert(value, clazz)
          if (null == rs) None else Some(rs)
        }
      case _ => None
    }
  }

  def getBoolean(data: Map[String, Any], name: String): Option[Boolean] = {
    get(data, name, classOf[Boolean])
  }

  def getDate(data: Map[String, Any], name: String): Option[Date] = {
    get(data, name, classOf[Date])
  }

  def getDateTime(data: Map[String, Any], name: String): Option[ju.Date] = {
    get(data, name, classOf[ju.Date])
  }

  def getFloat(data: Map[String, Any], name: String): Option[Float] = {
    get(data, name, classOf[Float])
  }

  def getInt(data: Map[String, Any], name: String): Option[Int] = {
    get(data, name, classOf[Int])
  }

  def getShort(data: Map[String, Any], name: String): Option[Short] = {
    get(data, name, classOf[Short])
  }

  def getLong(data: Map[String, Any], name: String): Option[Long] = {
    get(data, name, classOf[Long])
  }

  /**
   * 返回request中以prefix.开头的参数
   *
   * @param exclusiveAttrNames 要排除的属性串
   */
  def sub(data: Map[String, Any], prefix: String, exclusiveAttrNames: String): Map[String, Any] = {
    sub(data, prefix, exclusiveAttrNames, true)
  }

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
    for ((key, value) <- data) {
      if ((key.indexOf(prefix + ".") == 0) && (!excludes.contains(key))) {
        newParams.put((if (stripPrefix) key.substring(prefix.length + 1) else key), value)
      }
    }
    newParams.toMap
  }
}
