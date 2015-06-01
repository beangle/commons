/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.lang.annotation.Annotation
import java.lang.reflect.{ Method, ParameterizedType, Type, TypeVariable }

import scala.language.existentials

import org.beangle.commons.lang.{ Objects, Throwables }

object Reflections {

  def newInstance[T](clazz: Class[T]): T = {
    try {
      return clazz.newInstance()
    } catch {
      case e: Exception => Throwables.propagate(e)
    }
    Objects.default(clazz)
  }

  /**
   * Find parameter types of given class's interface or superclass
   */
  def getGenericParamType(clazz: Class[_], expected: Class[_]): collection.Map[String, Class[_]] = {
    if (!expected.isAssignableFrom(clazz)) return Map.empty
    if (expected.isInterface) {
      getInterfaceParamType(clazz, expected)
    } else {
      getParamType(clazz.getSuperclass(), clazz.getGenericSuperclass(), expected)
    }
  }

  private def getInterfaceParamType(clazz: Class[_], expected: Class[_],
    paramTypes: collection.Map[String, Class[_]] = Map.empty): collection.Map[String, Class[_]] = {
    val interfaces = clazz.getInterfaces
    val idx = (0 until interfaces.length) find { i => expected.isAssignableFrom(interfaces(i)) }
    idx match {
      case Some(i) => getParamType(interfaces(i), clazz.getGenericInterfaces()(i), expected, paramTypes)
      case _ => {
        val superClass = clazz.getSuperclass
        getInterfaceParamType(superClass, expected, getParamType(superClass, clazz.getGenericSuperclass, superClass, paramTypes))
      }
    }
  }

  private def getParamType(clazz: Class[_], tp: Type, expected: Class[_],
    paramTypes: collection.Map[String, Class[_]] = Map.empty): collection.Map[String, Class[_]] = {
    if (classOf[AnyRef] == clazz) return paramTypes

    val newParamTypes: collection.Map[String, Class[_]] = tp match {
      case ptSuper: ParameterizedType =>
        val tmp = new collection.mutable.HashMap[String, Class[_]]
        val ps = ptSuper.getActualTypeArguments
        val tvs = clazz.getTypeParameters
        (0 until ps.length) foreach { k =>
          val resultType = ps(k) match {
            case c: Class[_] => c
            case tv: TypeVariable[_] => paramTypes.get(tv.getName).orNull
            case pt: ParameterizedType => pt.getRawType().asInstanceOf[Class[_]]
          }
          if (null != resultType) tmp.put(tvs(k).getName, resultType)
        }
        tmp
      case _ => Map.empty
    }
    if (clazz == expected) {
      newParamTypes
    } else {
      if (clazz.isInterface) {
        getInterfaceParamType(clazz, expected, newParamTypes)
      } else {
        getParamType(clazz.getSuperclass(), clazz.getGenericSuperclass(), expected, newParamTypes)
      }
    }
  }

  def isAnnotationPresent[T <: Annotation](method: Method, clazz: Class[T]): Boolean = {
    val ann = method.getAnnotation(clazz)
    if (null == ann) {
      val delaringClass = method.getDeclaringClass
      if (delaringClass == classOf[Object]) return false
      val superClass = delaringClass.getSuperclass
      try {
        isAnnotationPresent(superClass.getMethod(method.getName(), method.getParameterTypes(): _*), clazz)
      } catch {
        case e: NoSuchMethodException => false
      }
    } else true
  }
  /**
   * Find annotation in method declare class hierarchy
   */
  def getAnnotation[T <: Annotation](method: Method, clazz: Class[T]): Tuple2[T, Method] = {
    val ann = method.getAnnotation(clazz)
    if (null == ann) {
      val delaringClass = method.getDeclaringClass
      if (delaringClass == classOf[Object]) return null.asInstanceOf[Tuple2[T, Method]]
      val superClass = delaringClass.getSuperclass
      try {
        getAnnotation(superClass.getMethod(method.getName(), method.getParameterTypes(): _*), clazz)
      } catch {
        case e: NoSuchMethodException => null.asInstanceOf[Tuple2[T, Method]]
      }
    } else (ann, method)
  }
}
