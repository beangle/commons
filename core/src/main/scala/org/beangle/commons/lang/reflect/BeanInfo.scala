/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.reflect

import java.lang.reflect.Method

import scala.language.existentials
import scala.reflect.runtime.{ universe => ru }

/**
 * defaultConstructorParams is 1 based
 */
class BeanInfo(val properties: Map[String, PropertyDescriptor],
               val constructors: List[ConstructorDescriptor], val defaultConstructorParams: Map[Int, Any],
               protected[reflect] val usingTypeReflection: Boolean) {

  def getGetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.getter
      case None    => None
    }
  }
  def getPropertyTypeInfo(property: String): Option[TypeInfo] = {
    properties.get(property) match {
      case Some(p) => Some(p.typeinfo)
      case None    => None
    }
  }

  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property) match {
      case Some(p) => Some(p.clazz)
      case None    => None
    }
  }

  def getSetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.setter
      case None    => None
    }
  }

  def readables: Map[String, PropertyDescriptor] = {
    properties.filter(p => p._2.readable)
  }
  def writables: Map[String, PropertyDescriptor] = {
    properties.filter(p => p._2.writable)
  }

  def getWritableProperties(): Set[String] = {
    properties.filter(e => e._2.writable).keySet
  }
}
