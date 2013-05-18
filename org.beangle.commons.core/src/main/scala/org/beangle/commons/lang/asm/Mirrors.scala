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
package org.beangle.commons.lang.asm

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.impl.ConvertUtils
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.reflect.MethodInfo
//remove if not needed
import scala.collection.JavaConversions._

object Mirrors {

  def isReadable(bean: AnyRef, name: String): Boolean = {
    null != ClassInfo.get(bean.getClass).getReader(name)
  }

  def isWriteable(bean: AnyRef, name: String): Boolean = {
    null != ClassInfo.get(bean.getClass).getWriter(name)
  }

  /**
   * @throws NoSuchMethodException
   * @param bean
   * @param name
   * @param value
   */
  def setProperty(bean: AnyRef, name: String, value: AnyRef) {
    val mirror = Mirror.get(bean.getClass)
    mirror.invoke(bean, mirror.classInfo.getWriteIndex(name), value)
  }

  def getProperty[T](bean: AnyRef, name: String): T = {
    val mirror = Mirror.get(bean.getClass)
    mirror.invoke(bean, mirror.classInfo.getReadIndex(name)).asInstanceOf[T]
  }

  def copyProperty(bean: AnyRef, name: String, value: Any, conversion: Conversion): Any = {
    val info = ClassInfo.get(bean.getClass)
    info.getPropertyType(name) match {
      case Some(clazz) => {
        val converted = conversion.convert(value, clazz)
        Mirror.get(bean.getClass).invoke(bean, info.getWriteIndex(name), converted)
        converted
      }
      case _ => null
    }
  }

  def copyProperty(bean: AnyRef, name: String, value: Any): Any = {
    val info = ClassInfo.get(bean.getClass)
    info.getPropertyType(name) match {
      case Some(clazz) =>
        val converted = ConvertUtils.convert(value, clazz)
        Mirror.get(bean.getClass).invoke(bean, info.getWriteIndex(name), converted)
        converted
      case _ => null
    }
  }

  def none(): Mirror = new NoneMirror()

  class NoneMirror extends Mirror {

    this.classInfo = new ClassInfo(CollectUtils.newArrayList[MethodInfo]())

    override def invoke(obj: AnyRef, methodIndex: Int, args: Any*): Any = null
  }
}
