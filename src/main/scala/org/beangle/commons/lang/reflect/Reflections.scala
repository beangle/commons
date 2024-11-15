/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.{ClassLoaders, Primitives}

import java.lang.annotation.Annotation
import java.lang.reflect.*
import scala.collection.immutable.ArraySeq
import scala.collection.{immutable, mutable}
import scala.language.existentials
import scala.reflect.ClassTag

object Reflections {

  def newInstance[T](clazz: Class[T]): T = {
    if (clazz.isInterface || Modifier.isAbstract(clazz.getModifiers)) {
      null.asInstanceOf[T]
    } else {
      try {
        if clazz.getDeclaredConstructors.length == 0 then Primitives.default(clazz)
        else clazz.getDeclaredConstructor().newInstance()
      } catch {
        case e: Exception => null.asInstanceOf[T]
      }
    }
  }

  def getField(clazz: Class[_], name: String): Option[Field] = {
    try {
      Some(clazz.getField(name))
    } catch {
      case e: NoSuchFieldException =>
        var superClass = clazz
        var res: Option[Field] = None
        while (res.isEmpty && null != superClass && superClass != classOf[AnyRef]) {
          res = getDeclaredField(superClass, name)
          superClass = superClass.getSuperclass
        }
        res
    }
  }

  private def getDeclaredField(clazz: Class[_], name: String): Option[Field] = {
    try {
      val f = clazz.getDeclaredField(name)
      f.setAccessible(true)
      Some(f)
    } catch
      case e: NoSuchFieldException => None
  }

  def newInstance[T](className: String): T = newInstance(className, null)

  def newInstance[T](className: String, classLoader: ClassLoader): T = {
    newInstance(ClassLoaders.load(className, classLoader).asInstanceOf[Class[T]])
  }

  def getInstance[T](name: String): T = {
    val companionClass = if name.endsWith("$") then name else name + "$"
    ClassLoaders.get(companionClass) match {
      case Some(clazz) =>
        if clazz.getConstructors.length > 0 then newInstance(clazz).asInstanceOf[T]
        else clazz.getDeclaredField("MODULE$").get(null).asInstanceOf[T]
      case None =>
        newInstance(ClassLoaders.load(name).asInstanceOf[Class[T]])
    }
  }

  /** Find parameter types of given class's interface or superclass
   */
  def getGenericParamTypes(clazz: Class[_], expected: Class[_]): collection.Map[String, Class[_]] = {
    if !expected.isAssignableFrom(clazz) then Map.empty else getGenericParamTypes(clazz, Set(expected))
  }

  def getCollectionParamTypes(clazz: Class[_]): ArraySeq[TypeInfo] = {
    val collections: Set[Class[_]] = Set(classOf[mutable.Seq[_]], classOf[immutable.Seq[_]], classOf[java.util.Collection[_]])
    val types = getGenericParamTypes(clazz, collections)
    if types.isEmpty then ArraySeq(TypeInfo.AnyRefType)
    else {
      if types.head._2 == clazz then ArraySeq(TypeInfo.GeneralType(clazz))
      else ArraySeq(TypeInfo.get(types.head._2, false))
    }
  }

  def getMapParamTypes(clazz: Class[_]): ArraySeq[TypeInfo] = {
    val maps: Set[Class[_]] = Set(classOf[mutable.Map[_, _]], classOf[immutable.Map[_, _]], classOf[java.util.Map[_, _]])
    val types = getGenericParamTypes(clazz, maps)
    if (types.isEmpty) then ArraySeq(TypeInfo.AnyRefType, TypeInfo.AnyRefType)
    else ArraySeq(TypeInfo.get(types("K"), false), TypeInfo.get(types("V"), false))
  }

  def getGenericParamTypes(clazz: Class[_], expects: Set[Class[_]]): collection.Map[String, Class[_]] = {
    var targetParamTypes: collection.Map[String, Class[_]] = Map.empty
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    var nextClass = clazz

    while (null != nextClass && classOf[AnyRef] != nextClass && targetParamTypes.isEmpty) {
      targetParamTypes = navIterface(nextClass, expects, paramTypes)
      if (targetParamTypes.isEmpty) {
        val nextType = nextClass.getGenericSuperclass
        nextClass = nextClass.getSuperclass
        paramTypes = Reflections.deduceParamTypes(nextClass, nextType, paramTypes)
        if (expects.contains(nextClass)) {
          targetParamTypes = paramTypes
        }
      }
    }
    targetParamTypes
  }

  private def isAssignableFrom(targets: Set[Class[_]], source: Class[_]): Boolean = {
    targets.exists(_.isAssignableFrom(source))
  }

  private def navIterface(clazz: Class[_],
                          targets: Set[Class[_]], paramTypes: collection.Map[String, Class[_]]): collection.Map[String, Class[_]] = {
    if (null == clazz || classOf[AnyRef] == clazz) return null;
    val interfaceTypes = clazz.getGenericInterfaces
    val canidateIterface = interfaceTypes.find { x =>
      x match {
        case pt: ParameterizedType => isAssignableFrom(targets, pt.getRawType.asInstanceOf[Class[?]])
        case c: Class[_] => isAssignableFrom(targets, c)
        case _ => false
      }
    }
    var result: collection.Map[String, Class[_]] = Map.empty
    canidateIterface foreach { ci =>
      ci match {
        case pt: ParameterizedType =>
          val interface = pt.getRawType.asInstanceOf[Class[_]]
          val newParamTypes = Reflections.deduceParamTypes(interface, pt, paramTypes)
          if (targets.contains(interface)) {
            result = newParamTypes
          } else {
            result = navIterface(interface, targets, newParamTypes)
          }
        case c: Class[_] =>
          if (!targets.contains(c)) result = navIterface(c, targets, paramTypes)
        case _ =>
      }
    }
    result
  }

  def isAnnotationPresent[T <: Annotation](method: Method, clazz: Class[T]): Boolean = {
    val ann = method.getAnnotation(clazz)
    if (null == ann) {
      val delaringClass = method.getDeclaringClass
      val superClass = delaringClass.getSuperclass
      if (null == superClass) return false
      try
        isAnnotationPresent(superClass.getMethod(method.getName, method.getParameterTypes: _*), clazz)
      catch {
        case e: NoSuchMethodException => false
      }
    } else true
  }

  /** Find annotation in method declare class hierarchy
   */
  def getAnnotation[T <: Annotation](method: Method, clazz: Class[T]): Tuple2[T, Method] = {
    val ann = method.getAnnotation(clazz)
    if (null == ann) {
      val delaringClass = method.getDeclaringClass
      val superClass = delaringClass.getSuperclass
      if (null == superClass) return null.asInstanceOf[Tuple2[T, Method]]
      try
        getAnnotation(superClass.getMethod(method.getName, method.getParameterTypes: _*), clazz)
      catch {
        case e: NoSuchMethodException => null.asInstanceOf[Tuple2[T, Method]]
      }
    } else (ann, method)
  }

  /** 得到类和对应泛型的参数信息
   */
  def deduceParamTypes(clazz: Class[_], typ: java.lang.reflect.Type,
                       paramTypes: collection.Map[String, Class[_]]): collection.Map[String, Class[_]] = {
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
