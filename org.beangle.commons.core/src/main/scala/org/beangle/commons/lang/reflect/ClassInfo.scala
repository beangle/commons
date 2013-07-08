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

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.util.Arrays
import java.util.Collection
import java.util.Collections

import scala.language.existentials
import scala.collection.JavaConversions._
import scala.collection.mutable

object ClassInfo {

  /**
   * class info cache
   */
  var cache = new mutable.HashMap[Class[_], ClassInfo]

  /**
   * Return true when Method is public and not static and not volatile.
   * <p>
   * javassist.util.proxy.ProxyFactory.getMethods has error due to bridge method.
   */
  private def goodMethod(method: Method): Boolean = {
    val modifiers = method.getModifiers
    if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) return false
    if (method.isBridge) return false
    val methodName = method.getName
    if (method.getParameterTypes.length == 0 &&
      (methodName == "hashCode" || methodName == "toString")) return false
    if (method.getParameterTypes.length == 1 & methodName == "equals") return false
    true
  }

  /**
   * Get ClassInfo by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_]): ClassInfo = {
    var exist = cache.get(clazz)
    if (exist.isDefined) return exist.get
    cache.synchronized {
      exist = cache.get(clazz)
      if (exist.isDefined) return exist.get
      val methods = new mutable.HashSet[MethodInfo]
      var nextClass = clazz
      var index = 0
      var nextParamTypes: collection.Map[String, Class[_]] = null
      while (null != nextClass && classOf[AnyRef] != nextClass) {
        val declaredMethods = nextClass.getDeclaredMethods
        var i = 0
        while (i < declaredMethods.length) {
          val method = declaredMethods(i)
          i += 1
          if (goodMethod(method)) {
            val types = method.getGenericParameterTypes
            val paramsTypes = new Array[Class[_]](types.length)
            for (j <- 0 until types.length) {
              val t = types(j)
              paramsTypes(j) =
                if (t.isInstanceOf[ParameterizedType])
                  t.asInstanceOf[ParameterizedType].getRawType.asInstanceOf[Class[_]]
                else if (t.isInstanceOf[TypeVariable[_]]) nextParamTypes.get(t.asInstanceOf[TypeVariable[_]].getName).get
                else t.asInstanceOf[Class[_]]
            }
            if (!methods.add(new MethodInfo(index, method, paramsTypes))) index -= 1
            index += 1
          }
        }
        val nextType = nextClass.getGenericSuperclass
        nextClass = nextClass.getSuperclass
        if (nextType.isInstanceOf[ParameterizedType]) {
          val tmp = new mutable.HashMap[String, Class[_]]
          val ps = nextType.asInstanceOf[ParameterizedType].getActualTypeArguments
          val tvs = nextClass.getTypeParameters
          for (k <- 0 until ps.length) {
            if (ps(k).isInstanceOf[Class[_]]) {
              tmp.put(tvs(k).getName, ps(k).asInstanceOf[Class[_]])
            } else if (ps(k).isInstanceOf[TypeVariable[_]]) {
              tmp.put(tvs(k).getName, nextParamTypes.get(ps(k).asInstanceOf[TypeVariable[_]].getName).get)
            }
          }
          nextParamTypes = tmp
        } else {
          nextParamTypes = Map.empty
        }
      }
      val newClassInfo = new ClassInfo(methods.toSeq)
      cache.put(clazz, newClassInfo)
      newClassInfo
    }
  }
}

/**
 * Class meta information.It contains method signature,property names
 *
 * @author chaostone
 * @since 3.2.0
 */
class ClassInfo(methodinfos: Seq[MethodInfo]) {

  private val methods = methodinfos.groupBy(info => info.method.getName)
  /**
   * unqiue method indexes,without any override
   */
  private val methodIndexs = methods.mapValues(ms => if (ms.size == 1) ms.head.index else -1).filter(e => e._2 > -1)

  /**
   * property read method indexes
   */
  val readers = findReaders(methodinfos)

  /**
   * property write method indexes
   */
  val writers = findWriters(methodinfos)

  private def findReaders(methodinfos: Seq[MethodInfo]): Map[String, MethodInfo] = {
    val readermap = new mutable.HashMap[String, MethodInfo]
    for (info <- methodinfos) {
      val property = info.property
      if(property.isDefined && property.get._1){
        val old = readermap.put(property.get._2, info)
        if(old.isDefined && info.method.getReturnType.isAssignableFrom(old.get.method.getReturnType))
          readermap += property.get._2 -> info
      }
    }
    Map.empty ++ readermap
  }

  private def findWriters(methodinfos: Seq[MethodInfo]): Map[String, MethodInfo] = {
    val writermap = new mutable.HashMap[String, MethodInfo]
    for (info <- methodinfos) {
      val property = info.property
      if(property.isDefined && !property.get._1) writermap += property.get._2 -> info
    }
    Map.empty ++ writermap
  }

  /**
   * Return property read index,return -1 when not found.
   */
  def getReadIndex(property: String): Int = {
    readers.get(property) match {
      case Some(method) => method.index
      case _ => -1
    }
  }

  /**
   * Return property read index,return -1 when not found.
   */
  def getReader(property: String): Option[MethodInfo] = readers.get(property)

  /**
   * Return property type,return null when not found.
   */
  def getPropertyType(property: String): Option[Class[_]] = {
    writers.get(property) match {
      case Some(method) => Some(method.parameterTypes(0))
      case _ => None
    }
  }

  /**
   * Return property write index,return -1 if not found.
   */
  def getWriteIndex(property: String): Int = {
    writers.get(property) match {
      case Some(method) => method.index
      case _ => -1
    }
  }

  /**
   * Return property write method,return null if not found.
   */
  def getWriter(property: String): Option[MethodInfo] = writers.get(property)

  /**
   * Return method index,return -1 if not found.
   */
  def getIndex(name: String, args: Any*): Int = {
    methodIndexs.get(name) match {
      case Some(index) => index
      case _ =>
        methods.get(name) match {
          case Some(exists) =>
            exists.find(_.matches(args)) match {
              case Some(info) => info.index
              case _ => -1
            }
          case _ => -1
        }
    }
  }

  /**
   * Return public metheds according to given name
   */
  def getMethods(name: String): Seq[MethodInfo] = {
    methods.get(name) match {
      case Some(ms) => ms
      case _ => Seq.empty
    }
  }

  /**
   * Return all public methods.
   */
  def getMethods(): Seq[MethodInfo] = {
    val methodInfos =new mutable.ListBuffer[MethodInfo]
    for ((key, value) <- methods;info <- value) methodInfos += info
    methodInfos.sorted.toList
  }

  def getWritableProperties(): Set[String] = writers.keySet
}
