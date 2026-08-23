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

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.{ClassMeta, Ctor, Method, Param, Property}
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.{Reflections, TypeInfo}

import java.lang.Character.isUpperCase
import java.lang.reflect.{Field, Method => JMethod, Modifier, ParameterizedType, TypeVariable}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.reflect.*

/** Runtime reflection counterpart of [[MetaDigger]]: reflects a Class into [[ClassMeta]].
  *
  * This is the fallback path when no pre-built metamodel.idx is available.
  * The returned ClassMeta is pure metadata (no MethodHandles); use
  * [[org.beangle.commons.lang.reflect.BeanInfo.from]] to reconstruct
  * a BeanInfo with accessor MethodHandles.
  *
  * {{{
  * val cm = MetaLoader.load(classOf[User])
  * val bi = BeanInfo.from(cm)  // adds MethodHandles
  * }}}
  */
object MetaLoader {

  private case class Accessor(method: JMethod, returnType: TypeInfo)

  /** Reflects a class into ClassMeta.
    *
    * Single-pass over class hierarchy: collects fields, getters/setters, and
    * non-accessor methods in one walk, avoiding repeated scans.
    */
  def load(clazz: Class[_]): ClassMeta = {
    val className = clazz.getName
    if (className.startsWith("java.") || className.startsWith("scala.") || className.contains("$$"))
      throw new RuntimeException("Cannot reflect class: " + clazz.getName)

    val isCase = TypeInfo.isCaseClass(clazz)
    val getters = new mutable.HashMap[String, Accessor]
    val setters = new mutable.HashMap[String, Accessor]
    val fields = new mutable.HashMap[String, Field]
    val nonAccessorMethods = new mutable.LinkedHashMap[String, Method] // dedup by name+sig
    val accessed = new mutable.HashSet[Class[_]]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty

    // Single pass: walk class hierarchy to discover fields, accessors, and methods
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      nextClass.getDeclaredFields foreach { f => fields += (f.getName -> f) }
      nextClass.getDeclaredMethods foreach { m =>
        processMethod(isCase, m, getters, setters, nonAccessorMethods, paramTypes)
      }
      navInterfaces(nextClass, accessed, getters, setters, nonAccessorMethods, paramTypes)
      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = Reflections.deduceParamTypes(nextClass, nextType, paramTypes)
    }

    // Discover constructors with default values
    val defaultCtorParamValues = findDefaultCtorParams(clazz)
    val ctors = discoverConstructors(clazz, defaultCtorParamValues, paramTypes)
    val primaryCtorParamNames = if ctors.isEmpty then Set.empty else ctors.head.parameters.map(_.name).toSet

    // Build property declarations from single-pass results
    val properties = buildProperties(getters, setters, fields, primaryCtorParamNames, isCase)

    ClassMeta(clazz, properties, ctors, nonAccessorMethods.values.toSeq)
  }

  /** Builds property declarations from discovered getters/setters. */
  private def buildProperties(
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    fields: mutable.HashMap[String, Field],
    primaryCtorParamNames: Set[String],
    isCase: Boolean
  ): Seq[Property] = {
    val allProps = getters.keySet ++ setters.keySet
    allProps.map { name =>
      val getter = getters.get(name)
      val setter = setters.get(name)
      val typeinfo = if getter.isEmpty then setter.get.returnType else getter.get.returnType
      val isTransientAnnotated = fields.get(name).exists(f => Modifier.isTransient(f.getModifiers))
      val isTransient = checkTransient(isTransientAnnotated, setter.isDefined, primaryCtorParamNames.contains(name))
      val getterName = getter.map(_.method.getName)
      val setterName = setter.map(_.method.getName)
      // Check for Option type
      typeinfo match
        case o: TypeInfo.OptionType => Property(name, o.elementType, isTransient, isOptional = true, getterName, setterName)
        case other => Property(name, other, isTransient, isOptional = false, getterName, setterName)
    }.toSeq.sortBy(_.name)
  }

  /** Discovers constructors with default values. */
  private def discoverConstructors(
    clazz: Class[_],
    defaultCtorParamValues: Map[Int, Any],
    paramTypes: collection.Map[String, Class[_]]
  ): Seq[Ctor] = {
    var foundDefaultCtor = false
    clazz.getConstructors.map { ctor =>
      val params = new mutable.ArrayBuffer[Param](ctor.getParameterCount)
      ctor.getParameters foreach { p =>
        params += Param(p.getName, typeof(p.getType, p.getParameterizedType, paramTypes), None)
      }
      if (!foundDefaultCtor && defaultCtorParamValues.nonEmpty) {
        if (isDefaultParamMatched(defaultCtorParamValues, params)) {
          foundDefaultCtor = true
          defaultCtorParamValues foreach { case (idx, v) =>
            params(idx - 1) = params(idx - 1).copy(defaultValue = Some(v))
          }
        }
      }
      Ctor(ArraySeq.from(params))
    }.toSeq
  }

  /** Finds default constructor parameter values from companion object. */
  private def findDefaultCtorParams(clazz: Class[_]): Map[Int, Any] = {
    org.beangle.commons.lang.ClassLoaders.get(clazz.getName + "$") match {
      case Some(companionClass) =>
        val singleton = companionClass.getDeclaredField("MODULE$").get(null)
        val params = Collections.newMap[Int, Any]
        companionClass.getDeclaredMethods foreach { m =>
          val index = Strings.substringAfter(m.getName, "$lessinit$greater$default$")
          if (Strings.isNotEmpty(index)) params.put(Integer.parseInt(index), m.invoke(singleton))
        }
        params.toMap
      case None => Map.empty
    }
  }

  private def isDefaultParamMatched(defaultCtorParamValues: Map[Int, Any], params: collection.Seq[Param]): Boolean = {
    defaultCtorParamValues.forall { case (idx, pv) =>
      if ((idx - 1) < params.length) {
        pv match
          case null => true
          case value => org.beangle.commons.lang.Primitives.wrap(params(idx - 1).typeinfo.clazz)
            .isAssignableFrom(org.beangle.commons.lang.Primitives.wrap(value.getClass))
      } else {
        false
      }
    }
  }

  private def navInterfaces(
    clazz: Class[_],
    accessed: mutable.HashSet[Class[_]],
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    nonAccessorMethods: mutable.LinkedHashMap[String, Method],
    paramTypes: collection.Map[String, Class[_]]
  ): Unit = {
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
        val interfaceParamTypes = Reflections.deduceParamTypes(interface, interfaceTypes(i), paramTypes)
        interface.getDeclaredMethods foreach { m =>
          processMethod(isCase, m, getters, setters, nonAccessorMethods, interfaceParamTypes)
        }
        navInterfaces(interface, accessed, getters, setters, nonAccessorMethods, paramTypes)
      }
    }
  }

  private def processMethod(
    isCase: Boolean,
    method: JMethod,
    getters: mutable.HashMap[String, Accessor],
    setters: mutable.HashMap[String, Accessor],
    nonAccessorMethods: mutable.LinkedHashMap[String, Method],
    paramTypes: collection.Map[String, Class[_]]
  ): Unit = {
    if (isFineMethod(isCase, method, false)) {
      findAccessor(method) match {
        case Some((readable, name)) =>
          if (readable) {
            val puttable = getters.get(name).forall(x => isJavaBeanGetter(x.method))
            if puttable then
              getters.put(name, Accessor(method, typeof(method.getReturnType, method.getGenericReturnType, paramTypes)))
          } else {
            val types = method.getGenericParameterTypes
            val clazzes = method.getParameterTypes
            val paramTypeInfos = new Array[TypeInfo](types.length)
            (0 until types.length) foreach { j => paramTypeInfos(j) = typeof(clazzes(j), types(j), paramTypes) }
            setters.put(name, Accessor(method, paramTypeInfos(0)))
          }
        case None =>
          // Non-accessor method: collect during walk
          val paramTypeInfos = method.getGenericParameterTypes.zip(method.getParameterTypes).map { (gt, pt) =>
            typeof(pt, gt, paramTypes)
          }
          val sig = method.getName + paramTypeInfos.map(_.clazz.getName).mkString("(", ",", ")")
          nonAccessorMethods.getOrElseUpdate(sig, Method(method.getName, paramTypeInfos.toSeq))
      }
    }
  }

  private def isJavaBeanGetter(method: JMethod): Boolean = {
    val name = method.getName
    if name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3)) then true
    else if name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2)) then true
    else false
  }

  /** Resolves TypeInfo from Class and generic Type. */
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
          case _: Class[_] => TypeInfo.get(clazz, false)
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

  // --- Method classification utilities (used by MetaLoader and MetaDigger) ---

  val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll") ++
    Set("apply", "unapply", "unApply", "canEqual")
  val caseIgnores = Set("productArity", "productIterator", "productPrefix", "productElement", "productElementName", "productElementNames", "copy")

  /** Returns true if property should be treated as transient. */
  def checkTransient(transientAnnotated: Boolean, hasSetter: Boolean, usedInPrimaryCtor: Boolean): Boolean = {
    if transientAnnotated then true else !usedInPrimaryCtor && !hasSetter
  }

  /** Returns true if method is a candidate for property accessor discovery. */
  def isFineMethod(isCase: Boolean, method: JMethod, allowBridge: Boolean = false): Boolean = {
    val modifiers = method.getModifiers
    val name = method.getName
    val ignored = ignores.contains(name) || (isCase && caseIgnores.contains(name))
    val modifierNice = !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
    !method.isAnnotationPresent(classOf[noreflect]) && !ignored && modifierNice && isFineMethodName(name) && (!method.isBridge || allowBridge)
  }

  /** Returns true if method name follows getter/setter convention. */
  private def isFineMethodName(name: String): Boolean = {
    if name.startsWith("_") then false
    else if name.endsWith("_$eq") then !name.substring(0, name.length - 4).contains("$")
    else !name.contains("$")
  }

  /** Returns (true, propertyName) for getter, (false, propertyName) for setter, or None. */
  def findAccessor(method: JMethod): Option[(Boolean, String)] = {
    val name = method.getName
    val parameterTypes = method.getParameterTypes
    if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
      Some((true, getPropertyName(name, true)))
    } else if (1 == parameterTypes.length) {
      val propertyName = getPropertyName(name, false)
      if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
    } else None
  }

  /** Extracts property name from getter/setter method name. */
  def getPropertyName(name: String, getter: Boolean): String = {
    if (getter) {
      if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
      else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) lower(name.substring(2))
      else name
    } else {
      if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
      else if (name.endsWith("_$eq")) Strings.substringBefore(name, "_$eq")
      else if (name.endsWith("_=")) Strings.substringBefore(name, "_=")
      else null
    }
  }

  private def lower(name: String): String = {
    if (name.length > 1 && isUpperCase(name.charAt(1))) name else Strings.uncapitalize(name)
  }
}
