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

import java.lang.reflect.{ Method, Modifier, ParameterizedType, TypeVariable }
import scala.collection.mutable
import scala.language.existentials
import org.beangle.commons.lang.Objects
import org.beangle.commons.collection.IdentityCache
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes

object ClassInfos {

  /**
   * Java Object's method
   */
  val reservedMethodNames = Set("hashCode", "equals", "toString", "wait", "notify", "notifyAll", "getClass")
  /**
   * class info cache
   */
  var cache = new IdentityCache[Class[_], ClassInfo]

  /**
   * Return true when Method is public and not static and not volatile.
   * <p>
   * javassist.util.proxy.ProxyFactory.getMethods has error due to bridge method.
   */
  private def goodMethod(method: Method): Boolean = {
    val modifiers = method.getModifiers
    if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) return false
    if (method.isBridge || reservedMethodNames.contains(method.getName)) return false
    true
  }

  /**
   * Load ClassInfo using reflections
   */
  def load(clazz: Class[_]): ClassInfo = {
    val methods = new mutable.HashSet[MethodInfo]
    val interfaceSets = new mutable.HashSet[Class[_]]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      val declaredMethods = nextClass.getDeclaredMethods
      (0 until declaredMethods.length) foreach { i =>
        val method = declaredMethods(i)
        if (goodMethod(method)) {
          val types = method.getGenericParameterTypes
          val paramsTypes = new Array[Class[_]](types.length)
          (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), paramTypes) }
          methods.add(new MethodInfo(method, paramsTypes, extract(method.getGenericReturnType, paramTypes)))
        }
      }

      navIterface(nextClass, interfaceSets, methods, paramTypes)

      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = deduceParamTypes(nextClass, nextType, paramTypes)
    }
    if (!Modifier.isPublic(clazz.getModifiers)) {
      methods.foreach { x => x.method.setAccessible(true) }
    }
    ClassInfo(methods.toSeq)
  }

  private def navIterface(clazz: Class[_], interfaceSets: mutable.HashSet[Class[_]],
                          methods: mutable.HashSet[MethodInfo],
                          paramTypes: collection.Map[String, Class[_]]): Unit = {
    if (null == clazz || classOf[AnyRef] == clazz) return ;
    val interfaceTypes = clazz.getGenericInterfaces
    val interfaces = clazz.getInterfaces
    (0 until interfaceTypes.length) foreach { i =>
      if (!interfaceSets.contains(interfaces(i))) {
        interfaceSets.add(interfaces(i))
        val interfaceParamTypes = deduceParamTypes(interfaces(i), interfaceTypes(i), paramTypes)
        val interface = interfaces(i)
        val declaredMethods = interface.getDeclaredMethods
        (0 until declaredMethods.length) foreach { i =>
          val method = declaredMethods(i)
          if (goodMethod(method)) {
            val types = method.getGenericParameterTypes
            val paramsTypes = new Array[Class[_]](types.length)
            (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), paramTypes) }
            methods.add(new MethodInfo(method, paramsTypes, extract(method.getGenericReturnType, paramTypes)))
          }
        }
        navIterface(interface, interfaceSets, methods, paramTypes)
      }
    }
  }

  private def extract(t: java.lang.reflect.Type, types: collection.Map[String, Class[_]]): Class[_] = {
    t match {
      case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
      case tv: TypeVariable[_]   => types.get(tv.getName).getOrElse(classOf[AnyRef])
      case c: Class[_]           => c
      case _                     => classOf[AnyRef]
    }
  }
  /**
   * Get ClassInfo from cache or load it by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_]): ClassInfo = {
    var exist = cache.get(clazz)
    if (null == exist) {
      exist = load(clazz)
      cache.put(clazz, exist)
    }
    exist
  }

}
