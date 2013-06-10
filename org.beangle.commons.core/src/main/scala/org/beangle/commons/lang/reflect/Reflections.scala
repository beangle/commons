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
package org.beangle.commons.lang.reflect

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.Objects

object Reflections {

  /**
   * Return the Java Class representing the property type of the specified
   * property, or <code>null</code> if there is no such property for the
   * specified bean.
   * this method using Introspector.getBeanInfo not lying on readmethod.
   * For generic super class read method's return type is more general.
   */
  def getPropertyType(clazz: Class[_], property: String): Class[_] = {
    var beanInfo: BeanInfo = null
    try {
      beanInfo = Introspector.getBeanInfo(clazz)
    } catch {
      case e: IntrospectionException => return null
    }
    val descriptors = beanInfo.getPropertyDescriptors
    if (null == descriptors) return null
    for (pd <- descriptors if pd.getName == property) return pd.getPropertyType
    null
  }

  /**
   * Return setter method.
   *
   * @param clazz
   * @param property
   * @return null when not found.
   */
  def getSetter(clazz: Class[_], property: String): Method = {
    val setName = "set" + Strings.capitalize(property)
    for (m <- clazz.getMethods if m.getName == setName) {
      if (!m.isBridge() && Modifier.isPublic(m.getModifiers) && !Modifier.isStatic(m.getModifiers) &&
        m.getParameterTypes.length == 1) {
        return m
      } else {
        return null
      }
    }
    null
  }

  /**
   * Return list of setters
   *
   * @param clazz
   */
  def getBeanSetters(clazz: Class[_]): List[Method] = {
    val methods = new collection.mutable.ListBuffer[Method]
    var i=0
    val allmethods=clazz.getMethods
    while(i < allmethods.size) {
      val m =allmethods(i)
      if (m.getName.startsWith("set") && m.getName.length > 3
        && Modifier.isPublic(m.getModifiers) && !Modifier.isStatic(m.getModifiers) &&
        m.getParameterTypes.length == 1)
        methods+=m
        i+=1
    }
    methods.toList
  }

  def newInstance[T](clazz: Class[T]): T = {
    try {
      return clazz.newInstance()
    } catch {
      case e: Exception => Throwables.propagate(e)
    }
    Objects.default(clazz)
  }
}
