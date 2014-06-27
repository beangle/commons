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
package org.beangle.commons.lang.reflect

import java.lang.reflect.{ Method, Modifier, ParameterizedType, TypeVariable }

import scala.collection.mutable
import org.beangle.commons.lang.Objects

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
   * Load ClassInfo using reflections
   */
  def load(clazz: Class[_]): ClassInfo = {
    val methods = new mutable.HashSet[MethodInfo]
    var nextClass = clazz
    var index = 0
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      val declaredMethods = nextClass.getDeclaredMethods
      (0 until declaredMethods.length) foreach { i =>
        val method = declaredMethods(i)
        if (goodMethod(method)) {
          val types = method.getGenericParameterTypes
          val paramsTypes = new Array[Class[_]](types.length)
          (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), paramTypes) }
          if (!methods.add(new MethodInfo(index, method, paramsTypes, extract(method.getGenericReturnType, paramTypes)))) index -= 1
          index += 1
        }
      }
      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = nextType match {
        case ptSuper: ParameterizedType =>
          val tmp = new mutable.HashMap[String, Class[_]]
          val ps = ptSuper.getActualTypeArguments
          val tvs = nextClass.getTypeParameters
          (0 until ps.length) foreach { k =>
            ps(k) match {
              case c: Class[_] => tmp.put(tvs(k).getName, c)
              case tv: TypeVariable[_] => tmp.put(tvs(k).getName, paramTypes(tv.getName))
            }
          }
          tmp
        case _ => Map.empty
      }
    }
    new ClassInfo(methods.toSeq)
  }

  private def extract(t: java.lang.reflect.Type, types: collection.Map[String, Class[_]]): Class[_] = {
    t match {
      case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
      case tv: TypeVariable[_] => types.get(tv.getName).getOrElse(classOf[AnyRef])
      case c: Class[_] => c
      case _ => classOf[AnyRef]
    }
  }
  /**
   * Get ClassInfo from cache or load it by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_]): ClassInfo = {
    var exist = cache.get(clazz)
    if (exist.isDefined) return exist.get
    cache.synchronized {
      exist = cache.get(clazz)
      if (exist.isDefined) return exist.get
      val newClassInfo = load(clazz)
      cache.put(clazz, newClassInfo)
      newClassInfo
    }
  }
}


/**
 * Class meta information.It contains method signature,property names
 */
class ClassInfo(methodinfos: Seq[MethodInfo]) {

  private val methods = methodinfos.groupBy(info => info.method.getName)
  /**
   * unqiue method indexes,without any override
   */
  private val methodIndexs = methods.mapValues(ms => if (ms.size == 1) ms.head.index else -1).filter(e => e._2 > -1)
  
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
    methods.get(name).getOrElse(Seq.empty)
  }

  /**
   * Return all public methods.
   */
  def getMethods(): Seq[MethodInfo] = {
    val methodInfos = new mutable.ListBuffer[MethodInfo]
    for ((key, value) <- methods; info <- value) methodInfos += info
    methodInfos.sorted.toList
  }

}

/**
 * Method name and return type and parameters type
 */
class MethodInfo(val index: Int, val method: Method, val parameterTypes: Array[Class[_]],val returnType:Class[_]) extends Ordered[MethodInfo] {

  override def compare(o: MethodInfo): Int = this.index - o.index

  def matches(args: Any*): Boolean = {
    if (parameterTypes.length != args.length) return false
    (0 until args.length).find { i =>
      null != args(i) && !parameterTypes(i).isInstance(args(i))
    }.isEmpty
  }

  override def toString(): String = {
    val returnType = method.getReturnType
    val sb = new StringBuilder()
    sb.append(if ((null == returnType)) "void" else returnType.getSimpleName)
    sb.append(' ').append(method.getName)
    if (parameterTypes.length == 0) {
      sb.append("()")
    } else {
      sb.append('(')
      for (t <- parameterTypes) sb.append(t.getSimpleName).append(",")
      sb.deleteCharAt(sb.length - 1).append(')')
    }
    sb.toString
  }

  override def hashCode(): Int = {
    var hash = 0
    for (t <- parameterTypes) hash += t.hashCode
    hash + method.getName.hashCode
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: MethodInfo => {
      val other = obj
      Objects.equalsBuilder().add(method.getName, other.method.getName)
        .add(parameterTypes, other.parameterTypes)
        .isEquals
    }
    case _ => false
  }
}
