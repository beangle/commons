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
package org.beangle.commons.bean

import java.lang.reflect.Array
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.reflect.MethodInfo
import org.beangle.commons.conversion.Conversion
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.logging.Logging
import scala.collection.Map
import scala.collection.mutable

object PropertyUtils extends Logging {

  private val resolver = new PropertyNameResolver()

  /**
   * @throws NoSuchMethodException
   * @param bean
   * @param name
   * @param value
   */
  def setProperty(bean: AnyRef, name: String, value: Any) {
    copyProperty(bean, name, value, null)
  }

  def getProperty[T](inputBean: Any, propertyName: String): T = {
    var result = inputBean
    var name = propertyName
    while (resolver.hasNested(name)) {
      val next = resolver.next(name)
      result =
        if (result.isInstanceOf[Map[_, _]]) getPropertyOfMapBean(result.asInstanceOf[Map[Any, _]], next)
        else if (resolver.isMapped(next)) getMappedProperty(result, next)
        else if (resolver.isIndexed(next)) getIndexedProperty(result, next)
        else getSimpleProperty(result, next)
      if (result == null) return null.asInstanceOf[T]
      name = resolver.remove(name)
    }
    result = if (result.isInstanceOf[Map[_, _]]) getPropertyOfMapBean(result.asInstanceOf[Map[Any, _]], name)
    else if (resolver.isMapped(name)) getMappedProperty(result, name)
    else if (resolver.isIndexed(name)) getIndexedProperty(result, name)
    else getSimpleProperty(result, name)
    result.asInstanceOf[T]
  }

  def copyProperty(bean: AnyRef, propertyName: String, value: Any, conversion: Conversion): Any = {
    var result: Any = bean
    var name = propertyName
    while (resolver.hasNested(name)) {
      val next = resolver.next(name);
      result =
        if (bean.isInstanceOf[Map[_, _]]) getPropertyOfMapBean(result.asInstanceOf[Map[Any, _]], next)
        else if (resolver.isMapped(next)) getMappedProperty(result, next)
        else if (resolver.isIndexed(next)) getIndexedProperty(result, next)
        else getSimpleProperty(result, next)

      if (result == null) throw new RuntimeException("Null property value for '" + name + "' on bean class '" + bean.getClass + "'");
      name = resolver.remove(name)
    }

    if (result.isInstanceOf[mutable.Map[_, _]]) {
      setPropertyOfMapBean(result.asInstanceOf[mutable.Map[Any, Any]], name, value);
    } else if (resolver.isMapped(name)) {
      setMappedProperty(result, name, value)
    } else if (resolver.isIndexed(name)) {
      return copyIndexedProperty(result, name, value, conversion)
    } else {
      return copySimpleProperty(result, name, value, conversion)
    }
    return value
  }

  def copyProperty(bean: AnyRef, name: String, value: AnyRef): Any = copyProperty(bean, name, value, DefaultConversion.Instance)

  def isWriteable(bean: AnyRef, name: String): Boolean = ClassInfo.get(bean.getClass).getWriter(name).isDefined

  def getPropertyType(clazz: Class[_], name: String): Class[_] = ClassInfo.get(clazz).getPropertyType(name).orNull

  def getWritableProperties(clazz: Class[_]): Set[String] = ClassInfo.get(clazz).getWritableProperties

  def getSimpleProperty[T](bean: Any, name: String): T = {
    ClassInfo.get(bean.getClass).getReader(name) match {
      case Some(info) => info.method.invoke(bean).asInstanceOf[T]
      case _ =>
        logger.warn("Cannot find get" + Strings.capitalize(name) + " in " + bean.getClass)
        null.asInstanceOf[T]
    }
  }

  private def getPropertyOfMapBean(bean: Map[Any, _], propertyName: String): Any = {
    var name = resolver.getProperty(propertyName)
    if (name == null || name.length == 0) name = resolver.getKey(propertyName)
    else name = propertyName
    bean.get(name).orNull
  }

  private def getMappedProperty(bean: Any, name: String): Any = {
    val key = resolver.getKey(name)
    if (key == null) {
      throw new IllegalArgumentException("Invalid mapped property '" + name + "'")
    }
    val value = getSimpleProperty[Map[Any, _]](bean, resolver.getProperty(name))
    if (null == value) null else value.get(key).orNull
  }

  private def getIndexedProperty(bean: Any, name: String): Any = {
    val index = resolver.getIndex(name)
    if (index < 0) {
      throw new IllegalArgumentException("Invalid indexed property '" + name + "'")
    }
    val value = getSimpleProperty[AnyRef](bean, resolver.getProperty(name))
    if (null == value) return null
    if (value.getClass.isArray) Array.get(value, index) else value.asInstanceOf[Seq[_]](index)
  }

  private def copySimpleProperty(bean: Any, name: String, value: Any, conversion: Conversion): Any = {
    val classInfo = ClassInfo.get(bean.getClass)
    val info = classInfo.getWriter(name) match {
      case Some(info) => {
        val converted = if (null == conversion) value else conversion.convert(value, classInfo.getPropertyType(name).get)
        info.method.invoke(bean, converted.asInstanceOf[Object])
        converted
      }
      case _ => {
        logger.warn("Cannot find {} set method in ", name, bean.getClass)
        null
      }
    }
  }

  private def copyIndexedProperty(bean: Any, name: String, value: Any, conversion: Conversion): Any = {
    var index = -1
    try {
      index = resolver.getIndex(name);
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException("Invalid indexed property '" + name + "' on bean class '" + bean.getClass + "'")
    }
    if (index < 0) throw new IllegalArgumentException("Invalid indexed property '" + name
      + "' on bean class '" + bean.getClass + "'")

    // Isolate the name
    val resolvedName = resolver.getProperty(name);
    var rs = if (resolvedName != null && resolvedName.length() >= 0) getSimpleProperty(bean, resolvedName) else bean

    var converted = value;
    if (rs.getClass.isArray) {
      if (null != conversion) converted = conversion.convert(value, rs.getClass().getComponentType());
      Array.set(rs, index, value);
    } else if (rs.isInstanceOf[mutable.Seq[_]]) {
      rs.asInstanceOf[mutable.Seq[Any]].update(index, value)
    }
    return converted
  }

  private def setMappedProperty(bean: Any, name: String, value: Any) {
    // Identify the key of the requested individual property
    var key: String = null
    try {
      key = resolver.getKey(name);
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException("Invalid mapped property '" + name + "' on bean class '" + bean.getClass() + "'")
    }
    if (key == null) throw new IllegalArgumentException("Invalid mapped property '" + name
      + "' on bean class '" + bean.getClass() + "'")

    // Isolate the name
    val resolvedName = resolver.getProperty(name);
    val rs = if (resolvedName != null && resolvedName.length() >= 0) getSimpleProperty(bean, resolvedName) else bean
    if (rs.isInstanceOf[mutable.Map[_, _]]) rs.asInstanceOf[mutable.Map[Any, Any]].put(key, value)
  }

  private def setPropertyOfMapBean(bean: mutable.Map[Any, Any], propertyName: String, value: Any) {
    var pname = propertyName
    if (resolver.isMapped(propertyName)) {
      val name = resolver.getProperty(propertyName)
      if (name == null || name.length() == 0)
        pname = resolver.getKey(propertyName)

    }
    if (resolver.isIndexed(pname) || resolver.isMapped(pname)) throw new IllegalArgumentException(
      "Indexed or mapped properties are not supported on" + " objects of type Map: " + pname)
    bean.put(pname, value)
  }

}
