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
import java.util.Map
import java.util.Set
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.impl.DefaultConversion
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
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
class MapConverter(val conversion: DefaultConversion) {

  /**
   * <p>
   * Constructor for MapConverter.
   * </p>
   */
  def this() {
    this(DefaultConversion.Instance)
  }

  /**
   * <p>
   * getAll.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param attr a {@link java.lang.String} object.
   * @return an array of {@link java.lang.Object} objects.
   */
  def getAll(data: Map[String, Any], attr: String): Array[Any] = data.get(attr).asInstanceOf[Array[Any]]

  /**
   * <p>
   * getAll.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param attr a {@link java.lang.String} object.
   * @param clazz a {@link java.lang.Class} object.
   * @param <T> a T object.
   * @return an array of T objects.
   */
  def getAll[T >: AnyRef: ClassTag](data: Map[String, Any], attr: String, clazz: Class[T]): Array[T] = {
    convert(data.get(attr).asInstanceOf[Array[AnyRef]], clazz)
  }

  /**
   * get parameter named attr
   *
   * @param attr a {@link java.lang.String} object.
   * @return single value or multivalue joined with comma
   * @param data a {@link java.util.Map} object.
   */
  def getString(data: Map[String, Any], attr: String): String = {
    val value = data.get(attr)
    if (null == value) return null
    if (!value.getClass.isArray) return value.toString
    val values = value.asInstanceOf[Array[String]]
    if (values.length == 1) values(0)
    else Strings.join(values, ",")
  }

  /**
   * get parameter named attr
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.lang.Object} object.
   */
  def get(data: Map[String, Any], name: String): Any = {
    val value = data.get(name)
    if (null == value) return null
    if (value.getClass.isArray) {
      val values = value.asInstanceOf[Array[AnyRef]]
      if (values.length == 1) return values(0)
    }
    value
  }

  /**
   * <p>
   * convert.
   * </p>
   *
   * @param value a {@link java.lang.Object} object.
   * @param clazz a {@link java.lang.Class} object.
   * @param <T> a T object.
   * @return a T object.
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
   * <p>
   * convert.
   * </p>
   *
   * @param datas an array of {@link java.lang.Object} objects.
   * @param clazz a {@link java.lang.Class} object.
   * @param <T> a T object.
   * @return an array of T objects.
   */
  def convert[T: ClassTag](datas: Array[AnyRef], clazz: Class[T]): Array[T] = {
    if (null == datas) return null
    val newDatas = new Array[T](datas.size);
    for (i <- 0 until datas.length) newDatas(i) = convert(datas(i), clazz)
    newDatas
  }

  /**
   * <p>
   * get.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @param clazz a {@link java.lang.Class} object.
   * @param <T> a T object.
   * @return a T object.
   */
  def get[T](data: Map[String, Any], name: String, clazz: Class[T]): T = convert(get(data, name), clazz)

  /**
   * <p>
   * getBoolean.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.lang.Boolean} object.
   */
  def getBool(data: Map[String, Any], name: String): Boolean = get(data, name, classOf[Boolean])

  def getBoolean(data: Map[String, Any], name: String): java.lang.Boolean = get(data, name, classOf[java.lang.Boolean])

  /**
   * <p>
   * getDate.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.sql.Date} object.
   */
  def getDate(data: Map[String, Any], name: String): java.sql.Date = get(data, name, classOf[java.sql.Date])

  /**
   * <p>
   * getDateTime.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.util.Date} object.
   */
  def getDateTime(data: Map[String, Any], name: String): Date = get(data, name, classOf[Date])

  /**
   * <p>
   * getFloat.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.lang.Float} object.
   */
  def getFloat(data: Map[String, Any], name: String): java.lang.Float = get(data, name, classOf[Float])

  /**
   * <p>
   * getInteger.
   * </p>
   */
  def getInteger(data: Map[String, Any], name: String): java.lang.Integer = get(data, name, classOf[Integer])

  /**
   * Get Short.
   */
  def getShort(data: Map[String, Any], name: String): java.lang.Short = get(data, name, classOf[Short])

  /**
   * <p>
   * getLong.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.lang.Long} object.
   */
  def getLong(data: Map[String, Any], name: String): java.lang.Long = get(data, name, classOf[Long])

  /**
   * 返回request中以prefix.开头的参数
   *
   * @param prefix a {@link java.lang.String} object.
   * @param exclusiveAttrNames
   *          要排除的属性串
   * @param data a {@link java.util.Map} object.
   * @return a {@link java.util.Map} object.
   */
  def sub(data: Map[String, Any], prefix: String, exclusiveAttrNames: String): Map[String, Any] = {
    sub(data, prefix, exclusiveAttrNames, true)
  }

  /**
   * <p>
   * sub.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param prefix a {@link java.lang.String} object.
   * @return a {@link java.util.Map} object.
   */
  def sub(data: Map[String, Any], prefix: String): Map[String, Any] = sub(data, prefix, null, true)

  /**
   * <p>
   * sub.
   * </p>
   *
   * @param data a {@link java.util.Map} object.
   * @param prefix a {@link java.lang.String} object.
   * @param exclusiveAttrNames a {@link java.lang.String} object.
   * @param stripPrefix a boolean.
   * @return a {@link java.util.Map} object.
   */
  def sub(data: Map[String, Any], prefix: String, exclusiveAttrNames: String, stripPrefix: Boolean): Map[String, Any] = {
    val excludes = CollectUtils.newHashSet[String]
    if (Strings.isNotEmpty(exclusiveAttrNames)) {
      val exclusiveAttrs = Strings.split(exclusiveAttrNames, ",")
      for (i <- 0 until exclusiveAttrs.length) excludes.add(exclusiveAttrs(i))
    }
    val newParams = CollectUtils.newHashMap[String, Any]
    for ((key, value) <- data) {
      val attr = key
      if ((attr.indexOf(prefix + ".") == 0) && (!excludes.contains(attr))) {
        newParams.put((if (stripPrefix) attr.substring(prefix.length + 1) else attr), this.get(data, attr))
      }
    }
    newParams
  }
}
