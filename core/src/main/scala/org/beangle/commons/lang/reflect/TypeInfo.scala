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

import java.lang.reflect.{ Constructor, Method, ParameterizedType }

import scala.language.existentials

case class Getter(val method: Method, val returnType: Class[_])

case class Setter(val method: Method, val parameterTypes: Array[Class[_]])

sealed trait TypeInfo {
  def clazz: Class[_]
  def isElement: Boolean = false
  def isCollection: Boolean = false
  def isMap: Boolean = false
  def optional: Boolean
}

object TypeInfo {

  def isCollectionType(clazz: Class[_]): Boolean = {
    !classOf[collection.Map[_, _]].isAssignableFrom(clazz) && classOf[collection.Iterable[_]].isAssignableFrom(clazz) ||
      classOf[java.util.Collection[_]].isAssignableFrom(clazz) ||
      clazz.isArray
  }
  def isMapType(clazz: Class[_]): Boolean = {
    classOf[collection.Map[_, _]].isAssignableFrom(clazz) || classOf[java.util.Map[_, _]].isAssignableFrom(clazz)
  }

  def of(clazz: Class[_]): TypeInfo = {
    of(clazz, clazz.getGenericSuperclass)
  }

  def of(clazz: Class[_], typ: java.lang.reflect.Type): TypeInfo = {
    import Reflections._
    if (isCollectionType(clazz) || isMapType(clazz)) {
      if (clazz.isArray) {
        CollectionType(clazz, clazz.getComponentType)
      } else {
        typ match {
          case pt: ParameterizedType =>
            if (pt.getActualTypeArguments.size == 1) CollectionType(clazz, typeAt(pt, 0))
            else MapType(clazz, typeAt(pt, 0), typeAt(pt, 1))
          case c: Class[_] => {
            if (classOf[collection.Map[_, _]].isAssignableFrom(clazz)) {
              val typeParams = getGenericParamType(c, classOf[collection.Map[_, _]])
              MapType(clazz, typeParams("K"), typeParams("V"))
            } else if (classOf[collection.Iterable[_]].isAssignableFrom(clazz)) {
              val paramTypes = getGenericParamType(c, classOf[collection.Iterable[_]])
              if (paramTypes.isEmpty) CollectionType(clazz, classOf[Any]) else CollectionType(clazz, paramTypes.head._2)
            } else if (classOf[java.util.Collection[_]].isAssignableFrom(clazz)) {
              val paramTypes = getGenericParamType(c, classOf[java.util.Collection[_]])
              if (paramTypes.isEmpty) CollectionType(clazz, classOf[Any]) else CollectionType(clazz, paramTypes.head._2)
            } else {
              val typeParams = getGenericParamType(c, classOf[java.util.Map[_, _]])
              if (typeParams.size < 2) {
                MapType(clazz, classOf[Any], classOf[Any])
              } else {
                MapType(clazz, typeParams("K"), typeParams("V"))
              }
            }
          }
          case _ => MapType(clazz, classOf[Any], classOf[Any])
        }
      }
    } else {
      if (clazz == classOf[Option[_]]) {
        val innerType = typ match {
          case pt: ParameterizedType =>
            if (pt.getActualTypeArguments.size == 1) typeAt(pt, 0) else classOf[AnyRef]
          case c: Class[_] => classOf[AnyRef]
        }
        ElementType(innerType, true)
      } else {
        ElementType(clazz, false)
      }
    }
  }

  def typeAt(typ: java.lang.reflect.Type, idx: Int): Class[_] = {
    typ match {
      case c: Class[_] => c
      case pt: ParameterizedType =>
        pt.getActualTypeArguments()(idx) match {
          case c: Class[_] => c
          case _           => classOf[AnyRef]
        }
      case _ => classOf[AnyRef]
    }
  }
}
case class ElementType(clazz: Class[_], optional: Boolean = false) extends TypeInfo {
  override def isElement: Boolean = true
}

case class CollectionType(val clazz: Class[_], val elementType: Class[_]) extends TypeInfo {
  def isSetType: Boolean = {
    classOf[collection.Set[_]].isAssignableFrom(clazz) || classOf[java.util.Set[_]].isAssignableFrom(clazz)
  }
  override def isCollection: Boolean = true

  override def optional: Boolean = {
    false
  }
}

case class MapType(val clazz: Class[_], val keyType: Class[_], valueType: Class[_]) extends TypeInfo {
  override def optional: Boolean = {
    false
  }
  override def isMap: Boolean = true
}

class PropertyDescriptor(val name: String, val typeinfo: TypeInfo, val getter: Option[Method], val setter: Option[Method], val isTransient: Boolean) {
  def writable: Boolean = {
    None != setter
  }

  def clazz: Class[_] = {
    typeinfo.clazz
  }

  def readable: Boolean = {
    None != getter
  }
}

class ConstructorDescriptor(val constructor: Constructor[_], val args: Vector[TypeInfo])
