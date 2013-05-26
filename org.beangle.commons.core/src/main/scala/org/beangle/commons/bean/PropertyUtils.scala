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
package org.beangle.commons.bean

import java.lang.reflect.Array
import java.util.List
import java.util.Map
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.impl.ConvertUtils
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.reflect.MethodInfo
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import scala.collection.JavaConversions._

object PropertyUtils {

  private val logger = LoggerFactory getLogger getClass

  private val resolver = new PropertyNameResolver()

  /**
   * @throws NoSuchMethodException
   * @param bean
   * @param name
   * @param value
   */
  def setProperty(bean: AnyRef, name: String, value: Any) {
    ClassInfo.get(bean.getClass).getWriter(name) match {
      case Some(info) =>
        try {
          info.method.invoke(bean, value.asInstanceOf[Object])
        } catch {
          case e: Exception => Throwables.propagate(e)
        }
      case _ =>
        logger.warn("Cannot find set" + Strings.capitalize(name) + " in " +
          bean.getClass)
    }
  }

  def getProperty[T](inputBean: Any, propertyName: String): T = {
    var result = inputBean
    var name = propertyName
    while (resolver.hasNested(name)) {
      val next = resolver.next(name)
      result =
        if (result.isInstanceOf[Map[_, _]]) getPropertyOfMapBean(result.asInstanceOf[Map[_, _]], next)
        else if (resolver.isMapped(next)) getMappedProperty(result, next)
        else if (resolver.isIndexed(next)) getIndexedProperty(result, next)
        else getSimpleProperty(result, next)
      if (result == null) return null.asInstanceOf[T]

      name = resolver.remove(name)
    }
    result = if (result.isInstanceOf[Map[_, _]]) getPropertyOfMapBean(result.asInstanceOf[Map[_, _]], name)
    else if (resolver.isMapped(name)) getMappedProperty(result, name)
    else if (resolver.isIndexed(name)) getIndexedProperty(result, name)
    else getSimpleProperty(result, name)
    result.asInstanceOf[T]
  }

  def copyProperty(bean: AnyRef, name: String, value: Any, conversion: Conversion): Any = {
    val classInfo = ClassInfo.get(bean.getClass)
    classInfo.getWriter(name) match {
      case Some(info) => {
        val converted = conversion.convert(value, classInfo.getPropertyType(name).get)
        info.method.invoke(bean, converted.asInstanceOf[Object])
        converted
      }
      case _ => {
        logger.warn("Cannot find {} set method in ", name, bean.getClass)
        null
      }
    }
  }

  def copyProperty(bean: AnyRef, name: String, value: AnyRef) {
    val classInfo = ClassInfo.get(bean.getClass)
    val info = classInfo.getWriter(name) match {
      case Some(info) => {
        val converted = ConvertUtils.convert(value, classInfo.getPropertyType(name).get)
        info.method.invoke(bean, converted.asInstanceOf[Object])
        converted
      }
      case _ => {
        logger.warn("Cannot find {} set method in ", name, bean.getClass)
        null
      }
    }
  }

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

  private def getPropertyOfMapBean(bean: Map[_, _], propertyName: String): Any = {
    var name = resolver.getProperty(propertyName)
    if (name == null || name.length == 0) name = resolver.getKey(propertyName)
    else name = propertyName
    bean.get(name)
  }

  private def getMappedProperty(bean: Any, name: String): Any = {
    val key = resolver.getKey(name)
    if (key == null) {
      throw new IllegalArgumentException("Invalid mapped property '" + name + "'")
    }
    val value = getSimpleProperty[Map[_, _]](bean, resolver.getProperty(name))
    if (null == value) null else value.get(key)
  }

  private def getIndexedProperty(bean: Any, name: String): Any = {
    val index = resolver.getIndex(name)
    if (index < 0) {
      throw new IllegalArgumentException("Invalid indexed property '" + name + "'")
    }
    val value = getSimpleProperty[AnyRef](bean, resolver.getProperty(name))
    if (null == value) return null
    if (!value.getClass.isArray) (Array.get(value, index)) else value.asInstanceOf[List[_]].get(index)
  }
}
