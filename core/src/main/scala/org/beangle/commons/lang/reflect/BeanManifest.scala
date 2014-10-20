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

import java.lang.Character.isUpperCase
import java.lang.reflect.{ Method, Modifier, ParameterizedType, TypeVariable }
import scala.collection.mutable
import scala.language.existentials
import org.beangle.commons.lang.Strings.{ substringAfter, substringBefore, uncapitalize }
import org.beangle.commons.collection.IdentityCache

case class Getter(val method: Method, val returnType: Class[_])

case class Setter(val method: Method, val parameterTypes: Array[Class[_]])

class BeanManifest(val getters: Map[String, Getter], val setters: Map[String, Setter]) {

  def getGetter(property: String): Option[Getter] = getters.get(property)

  def getPropertyType(property: String): Option[Class[_]] = {
    getters.get(property).map(m => m.returnType)
  }

  def getSetter(property: String): Option[Setter] = setters.get(property)

  def getWritableProperties(): Set[String] = setters.keySet
}

object BeanManifest {

  private val cache = new IdentityCache[Class[_], BeanManifest]
  /**
   * Support scala case class
   */
  private val ignores = Set("hashCode", "toString", "productArity", "productPrefix", "productIterator")
  /**
   * Get BeanManifest from cache or load it by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_]): BeanManifest = {
    var exist = cache.get(clazz)
    if (null == exist) {
      exist = load(clazz)
      cache.put(clazz, exist)
    }
    exist
  }

  /**
   * Load BeanManifest using reflections
   */
  def load(clazz: Class[_]): BeanManifest = {
    val getters = new mutable.HashMap[String, Getter]
    val setters = new mutable.HashMap[String, Setter]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    val fields = new mutable.HashSet[String]
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      val declaredMethods = nextClass.getDeclaredMethods
      nextClass.getDeclaredFields() foreach { f => fields += f.getName }
      (0 until declaredMethods.length) foreach { i =>
        val method = declaredMethods(i)
        findAccessor(method) match {
          case Some(Tuple2(readable, name)) =>
            if (readable) {
              getters.put(name, Getter(method, extract(method.getGenericReturnType, paramTypes)))
            } else {
              val types = method.getGenericParameterTypes
              val paramsTypes = new Array[Class[_]](types.length)
              (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), paramTypes) }
              setters.put(name, Setter(method, paramsTypes))
            }
          case None =>
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
            tmp.put(tvs(k).getName,
              ps(k) match {
                case c: Class[_] => c
                case tv: TypeVariable[_] => paramTypes(tv.getName)
                case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
              })
          }
          tmp
        case _ => Map.empty
      }
    }
    val filterGetters = getters.filter {
      case (name, getter) =>
        setters.contains(name) || fields.contains(name)
    }
    new BeanManifest(filterGetters.toMap, setters.toMap)
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
   * Return this method is property read method (true,name) or write method(false,name) or None.
   */
  private def findAccessor(method: Method): Option[Tuple2[Boolean, String]] = {
    val modifiers = method.getModifiers
    if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || method.isBridge) return None

    val name = method.getName
    if (name.contains("$") && !name.contains("_$eq") || ignores.contains(name)) return None

    val parameterTypes = method.getParameterTypes
    if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
      val propertyName =
        if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) uncapitalize(substringAfter(name, "get"))
        else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) uncapitalize(substringAfter(name, "is"))
        else name
      Some((true, propertyName))
    } else if (1 == parameterTypes.length) {
      val propertyName =
        if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3)))
          uncapitalize(substringAfter(name, "set"))
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else null

      if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
    } else None
  }
}
