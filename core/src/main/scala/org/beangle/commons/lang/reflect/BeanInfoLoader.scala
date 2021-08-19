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

import java.beans.Transient
import java.lang.Character.isUpperCase
import java.lang.reflect.{Field, Method, Modifier, ParameterizedType, TypeVariable}
import org.beangle.commons.collection.{Collections, IdentityCache}
import org.beangle.commons.lang.Strings.{substringBefore, uncapitalize}
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes
import org.beangle.commons.lang.{ClassLoaders, Strings}

import scala.collection.mutable
import scala.collection.immutable.ArraySeq
import scala.reflect.*
import org.beangle.commons.lang.reflect.BeanInfo
import org.beangle.commons.lang.reflect.BeanInfo.*
import org.beangle.commons.lang.reflect.BeanInfo.Builder.filterSameNames

/** Load ClassInfo using reflection
  *
  */
object BeanInfoLoader {
  private case class Getter(method: Method, returnType: TypeInfo)

  private case class Setter(method: Method, parameterTypes: Array[TypeInfo])

  /**
    * Load BeanInfo using reflections
    */
  def load(clazz: Class[_]): BeanInfo = {
    val className = clazz.getName
    if (className.startsWith("java.") || className.startsWith("scala."))
      throw new RuntimeException("Cannot reflect class:" + clazz.getName)
    val originBeanInfo: Option[BeanInfo] = None
    //      if (className.contains("$$") && className.startsWith(clazz.getSuperclass.getName))
    //        Option(cache.get(clazz.getSuperclass))
    //      else
    //        None
    val isCase = TypeInfo.isCaseClass(clazz)
    val getters = new mutable.HashMap[String, Getter]
    val setters = new mutable.HashMap[String, Setter]
    val fields = new mutable.HashMap[String, Field]
    val methods = new mutable.HashSet[MethodInfo]
    val accessed = new mutable.HashSet[Class[_]]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      val declaredMethods = nextClass.getDeclaredMethods
      nextClass.getDeclaredFields foreach { f => fields += (f.getName -> f) }
      (0 until declaredMethods.length) foreach { i =>
        val method = declaredMethods(i)
        processMethod(isCase, method, getters, setters, methods, paramTypes)
      }
      navIterface(nextClass, accessed, getters, setters, methods, paramTypes)

      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = deduceParamTypes(nextClass, nextType, paramTypes)
    }

    //find default constructor parameters
    val defaultConstructorParams: Map[Int, Any] =
      ClassLoaders.get(clazz.getName + "$") match {
        case Some(oclazz) =>
          val osinglton = oclazz.getDeclaredField("MODULE$").get(null)
          val params = Collections.newMap[Int, Any]
          oclazz.getDeclaredMethods foreach { m =>
            val index = Strings.substringAfter(m.getName, "$lessinit$greater$default$")
            if (Strings.isNotEmpty(index)) params.put(Integer.parseInt(index), m.invoke(osinglton))
          }
          params.toMap
        case None => Map.empty
      }

    // find constructor with arguments
    val ctors = Collections.newBuffer[BeanInfo.ConstructorInfo]
    val pCtorParamNames = Collections.newSet[String]
    var i = 0
    clazz.getConstructors foreach { ctor =>
      val params = new mutable.ArrayBuffer[ParamInfo](ctor.getParameterCount)
      ctor.getParameters foreach { p => params += ParamInfo(p.getName, typeof(p.getType, p.getParameterizedType, paramTypes), None) }
      if (i == 0 && defaultConstructorParams.nonEmpty) {
        defaultConstructorParams foreach { case (i, v) =>
          params(i) = params(i).copy(defaultValue = Some(v))
          pCtorParamNames += params(i).name
        }
      }
      i += 1
      ctors += BeanInfo.ConstructorInfo(ArraySeq.from(params))
    }

    // make buffer to list and filter duplicated bridge methods
    val filterMethods = methods.groupBy(_.method.getName).map { case (x, sameNames) =>
      val nb = Builder.filterSameNames(sameNames)
      if (getters.contains(x) && nb.size == 1) getters.put(x, getters(x).copy(method = nb.head.method))
      (x, ArraySeq.from(nb))
    }
    // organize setter and getter
    val allprops = getters.keySet ++ setters.keySet
    val properties = Collections.newMap[String, BeanInfo.PropertyInfo]
    allprops foreach { p =>
      val getter = getters.get(p)
      val setter = setters.get(p)
      val typeinfo = if getter.isEmpty then setter.get.parameterTypes(0) else getter.get.returnType
      val isTransientAnnoted = fields.get(p).map(x => Modifier.isTransient(x.getModifiers)).getOrElse(false)
      val isTransient = BeanInfo.Builder.isTransient(isTransientAnnoted, setter.isDefined, pCtorParamNames.contains(p))
      val pd = BeanInfo.PropertyInfo(p, typeinfo, getter.map(x => x.method), setter.map(x => x.method), isTransient)
      properties.put(p, pd)
    }

    // change accessible for none public class
    if (!Modifier.isPublic(clazz.getModifiers))
      properties foreach { case (k, v) =>
        v.getter.foreach { m => m.setAccessible(true) }
        v.setter.foreach { m => m.setAccessible(true) }
      }
    new BeanInfo(clazz, ArraySeq.from(ctors), properties.toMap, filterMethods)
  }

  private def navIterface(clazz: Class[_], accessed: mutable.HashSet[Class[_]],
                          getters: mutable.HashMap[String, Getter], setters: mutable.HashMap[String, Setter],
                          methods: mutable.HashSet[MethodInfo],
                          paramTypes: collection.Map[String, Class[_]]): Unit = {
    if (null == clazz || classOf[AnyRef] == clazz) return
    val isCase = TypeInfo.isCaseClass(clazz)
    val interfaceTypes = clazz.getGenericInterfaces
    (0 until interfaceTypes.length) foreach { i =>
      val interface = interfaceTypes(i) match {
        case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
        case c: Class[_] => c
      }
      if (!accessed.contains(interface)) {
        accessed.add(interface)
        val interfaceParamTypes = deduceParamTypes(interface, interfaceTypes(i), paramTypes)
        val declaredMethods = interface.getDeclaredMethods
        (0 until declaredMethods.length) foreach { i =>
          val method = declaredMethods(i)
          processMethod(isCase, method, getters, setters, methods, interfaceParamTypes)
        }
        navIterface(interface, accessed, getters, setters, methods, paramTypes)
      }
    }
  }

  private def processMethod(isCase: Boolean, method: Method, getters: mutable.HashMap[String, Getter],
                            setters: mutable.HashMap[String, Setter],
                            methods: mutable.HashSet[MethodInfo],
                            paramTypes: collection.Map[String, Class[_]]): Unit = {
    if (BeanInfo.Builder.isFineMethod(isCase, method, false)) {
      BeanInfo.Builder.findAccessor(method) match {
        case Some(Tuple2(readable, name)) =>
          if (readable) {
            val puttable = getters.get(name).forall(x => isJavaBeanGetter(x.method)) //FIXME
            if (puttable)
              getters.put(name, Getter(method, typeof(method.getReturnType, method.getGenericReturnType, paramTypes)))
          } else {
            val types = method.getGenericParameterTypes
            val clazzes = method.getParameterTypes
            val paramsTypes = new Array[TypeInfo](types.length)
            (0 until types.length) foreach { j => paramsTypes(j) = typeof(clazzes(j), types(j), paramTypes) }
            setters.put(name, Setter(method, paramsTypes))
          }
        case None =>
      }
      val types = method.getGenericParameterTypes
      val params = new mutable.ArrayBuffer[ParamInfo](types.length)
      method.getParameters foreach { p => params += ParamInfo(p.getName, typeof(p.getType, p.getParameterizedType, paramTypes), None) }
      methods.add(MethodInfo(method, typeof(method.getReturnType, method.getGenericReturnType, paramTypes), ArraySeq.from(params)))
    }
  }

  private def isJavaBeanGetter(method: Method): Boolean = {
    val name = method.getName
    if name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3)) then true
    else if name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2)) then true
    else false
  }

  def typeof(clazz: Class[_], typ: java.lang.reflect.Type, paramTypes: collection.Map[String, Class[_]]): TypeInfo = {
    if TypeInfo.isIterableType(clazz) then
      if clazz.isArray then
        TypeInfo.get(clazz, clazz.getComponentType)
      else
        typ match {
          case pt: ParameterizedType =>
            if (pt.getActualTypeArguments.length == 1) TypeInfo.get(clazz, typeAt(pt, 0))
            else TypeInfo.get(clazz, typeAt(pt, 0), typeAt(pt, 1))
          case tv: TypeVariable[_] => TypeInfo.get(paramTypes.getOrElse(tv.getName, classOf[AnyRef]))
          case c: Class[_] => TypeInfo.get(clazz, false)
          case _ => TypeInfo.get(clazz, classOf[Any], classOf[Any])
        }
    else if clazz == classOf[Option[_]] then
      val innerType = typ match {
        case pt: ParameterizedType => if (pt.getActualTypeArguments.length == 1) typeAt(pt, 0) else classOf[AnyRef]
        case c: Class[_] => classOf[AnyRef]
      }
      TypeInfo.get(innerType, optional = true)
    else
      TypeInfo.get(clazz)
  }

  private def typeAt(typ: java.lang.reflect.Type, idx: Int): Class[_] = {
    typ match {
      case c: Class[_] => c
      case pt: ParameterizedType =>
        pt.getActualTypeArguments()(idx) match {
          case c: Class[_] => c
          case _ => classOf[AnyRef]
        }
      case _ => classOf[AnyRef]
    }
  }

}
