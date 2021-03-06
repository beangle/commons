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

import java.lang.annotation.Annotation
import java.lang.reflect.{Method, ParameterizedType, Type, TypeVariable}

import org.beangle.commons.lang.{ClassLoaders, Objects, Throwables}

import scala.language.existentials
import scala.reflect.ClassTag

object Reflections {

  def newInstance[T](clazz: Class[T]): T = {
    try {
      clazz.getDeclaredConstructor().newInstance()
    } catch {
      case e: Exception =>
        Throwables.propagate(e)
        Objects.default(clazz)
    }
  }

  def newInstance[T](className: String, classLoader: ClassLoader = null): T = {
    ClassLoaders.load(className, classLoader).getDeclaredConstructor().newInstance().asInstanceOf[T]
  }

  def getInstance[T: ClassTag](name: String)(implicit manifest: Manifest[T]): T = {
    var moduleClass = ClassLoaders.load(name)
    if (!manifest.runtimeClass.isAssignableFrom(moduleClass)) {
      ClassLoaders.get(name + "$") match {
        case Some(clazz) => moduleClass = clazz
        case None => throw new RuntimeException(name + " is not a module")
      }
    }
    if (moduleClass.getConstructors.length > 0) {
      moduleClass.getDeclaredConstructor().newInstance().asInstanceOf[T]
    } else {
      moduleClass.getDeclaredField("MODULE$").get(null).asInstanceOf[T]
    }
  }

  /**
    * Find parameter types of given class's interface or superclass
    */
  def getGenericParamType(clazz: Class[_], expected: Class[_]): collection.Map[String, Class[_]] = {
    if (!expected.isAssignableFrom(clazz)) return Map.empty
    if (expected.isInterface) {
      getInterfaceParamType(clazz, expected)
    } else {
      getParamType(clazz.getSuperclass, clazz.getGenericSuperclass, expected)
    }
  }

  private def getInterfaceParamType(clazz: Class[_], expected: Class[_],
                                    paramTypes: collection.Map[String, Class[_]] = Map.empty): collection.Map[String, Class[_]] = {
    require(clazz != null, "clazz is null")
    val interfaces = clazz.getInterfaces
    val idx = (0 until interfaces.length) find { i => expected.isAssignableFrom(interfaces(i)) }
    idx match {
      case Some(i) =>
        val gis = clazz.getGenericInterfaces
        //如果此时出现genericInterfaces<interfaces,表示子类、父类均实现了接口，但是子类的class中好像没有范型信息，估计是scala编译导致的。
        if (gis.length > i) {
          getParamType(interfaces(i), gis(i), expected, paramTypes)
        } else {
          val superClass = clazz.getSuperclass
          if (null == superClass) {
            Map.empty
          } else {
            getInterfaceParamType(superClass, expected, getParamType(superClass, clazz.getGenericSuperclass, superClass, paramTypes))
          }
        }
      case _ =>
        val superClass = clazz.getSuperclass
        if (null == superClass) {
          Map.empty
        } else {
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
            case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
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
        getParamType(clazz.getSuperclass, clazz.getGenericSuperclass, expected, newParamTypes)
      }
    }
  }

  def isAnnotationPresent[T <: Annotation](method: Method, clazz: Class[T]): Boolean = {
    val ann = method.getAnnotation(clazz)
    if (null == ann) {
      val delaringClass = method.getDeclaringClass
      val superClass = delaringClass.getSuperclass
      if (null == superClass) return false
      try {
        isAnnotationPresent(superClass.getMethod(method.getName, method.getParameterTypes: _*), clazz)
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
      val superClass = delaringClass.getSuperclass
      if (null == superClass) return null.asInstanceOf[Tuple2[T, Method]]
      try {
        getAnnotation(superClass.getMethod(method.getName, method.getParameterTypes: _*), clazz)
      } catch {
        case e: NoSuchMethodException => null.asInstanceOf[Tuple2[T, Method]]
      }
    } else (ann, method)
  }

  /**
    * 得到类和对应泛型的参数信息
    */
  def deduceParamTypes(clazz: Class[_], typ: java.lang.reflect.Type, paramTypes: collection.Map[String, Class[_]]): collection.Map[String, Class[_]] = {
    typ match {
      case ptSuper: ParameterizedType =>
        val tmp = new collection.mutable.HashMap[String, Class[_]]
        val ps = ptSuper.getActualTypeArguments
        val tvs = clazz.getTypeParameters
        (0 until ps.length) foreach { k =>
          val paramType = ps(k) match {
            case c: Class[_] => Some(c)
            case tv: TypeVariable[_] => paramTypes.get(tv.getName)
            case pt: ParameterizedType => Some(pt.getRawType.asInstanceOf[Class[_]])
          }
          paramType foreach (pt => tmp.put(tvs(k).getName, pt))
        }
        tmp
      case _ => Map.empty
    }
  }
}
