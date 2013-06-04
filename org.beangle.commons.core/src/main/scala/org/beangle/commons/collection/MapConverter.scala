/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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

import java.util.Date
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.impl.DefaultConversion
import scala.reflect.ClassTag
import org.beangle.commons.lang.Objects

/**
 * <p>
 * MapConverter class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class MapConverter(val conversion: DefaultConversion=DefaultConversion.Instance) {

  /**
   * getAll.
   */
  def getAll(data: Map[String, Any], attr: String): Array[Any] = data.get(attr).asInstanceOf[Array[Any]]

  /**
   * getAll.
   */
  def getAll[T >: AnyRef: ClassTag](data: Map[String, Any], attr: String, clazz: Class[T]): Array[T] = {
    convert(data.get(attr).asInstanceOf[Array[AnyRef]], clazz)
  }

  /**
    * get parameter named attr
    */
  def getString(data: Map[String, Any], attr: String): Option[String] = {
    data.get(attr) match {
      case Some(value) =>
        if(null==value) None
        else{
          if (value.getClass.isArray){
            val values = value.asInstanceOf[Array[String]]
            if (values.length == 1) Some(values(0))
            else Some(Strings.join(values, ","))
          }
          else  Some(value.toString)
        }
      case _ => None
    }
  }

  /**
    * get parameter named attr
    */
  def get(data: Map[String, Any], name: String): Option[Any] = {
    data.get(name) match{
      case Some(value)=>
        if(null==value) None
        else{
          if (value.getClass.isArray) {
            val values = value.asInstanceOf[Array[Any]]
            if (values.length == 1) Some(values(0)) else Some(values)
          }else Some(value)
        }
      case _ => None
    }
  }

  /**
   * convert.
   */
  def convert[T](value: Any, clazz: Class[T]): T = {
    if (null == value) return Objects.default(clazz)
    if (value.isInstanceOf[String] && Strings.isEmpty(value.asInstanceOf[String])) return Objects.default(clazz)
    var inputValue = value;
    if (value.getClass.isArray) {
      val values = value.asInstanceOf[Array[Any]]
      if (values.length >= 1) inputValue = values(0)
    }
    conversion.convert(inputValue, clazz)
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
  def get[T](data: Map[String, Any], name: String, clazz: Class[T]): Option[T] ={
    get(data, name) match{
      case Some(value) => Some(convert(value,clazz))
      case _ => None
    }
  }

  /**
   * getBoolean.
   */
  def getBoolean(data: Map[String, Any], name: String): Option[Boolean] = get(data, name, classOf[Boolean])

  /**
   * getDate.
   */
  def getDate(data: Map[String, Any], name: String): Option[java.sql.Date] = get(data, name, classOf[java.sql.Date])

  /**
   * getDateTime.
   */
  def getDateTime(data: Map[String, Any], name: String): Option[Date] = get(data, name, classOf[Date])

  /**
   * getFloat.
   */
  def getFloat(data: Map[String, Any], name: String):Option[Float] = get(data, name, classOf[Float])

  /**
   * <p>
   * getInteger.
   * </p>
   */
  def getInteger(data: Map[String, Any], name: String):Option[Integer] = get(data, name, classOf[Integer])

  /**
   * Get Short.
   */
  def getShort(data: Map[String, Any], name: String):Option[Short] = get(data, name, classOf[Short])

  /**
   * getLong.
   */
  def getLong(data: Map[String, Any], name: String):Option[Long] = get(data, name, classOf[Long])

  /**
   * 返回request中以prefix.开头的参数
   *
   * @param exclusiveAttrNames
   *          要排除的属性串
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
    val excludes = CollectUtils.newHashSet[String]
    if (Strings.isNotEmpty(exclusiveAttrNames)) {
      val exclusiveAttrs = Strings.split(exclusiveAttrNames, ",")
      for (i <- 0 until exclusiveAttrs.length) excludes.add(exclusiveAttrs(i))
    }
    val newParams = new collection.mutable.HashMap[String, Any]
    for ((key, value) <- data) {
      val attr = key
      if ((attr.indexOf(prefix + ".") == 0) && (!excludes.contains(attr))) {
        newParams.put((if (stripPrefix) attr.substring(prefix.length + 1) else attr), this.get(data, attr))
      }
    }
    newParams.toMap
  }
}
