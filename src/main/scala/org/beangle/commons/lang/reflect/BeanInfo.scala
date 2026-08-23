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

import org.beangle.commons.bean.meta.MetaModel
import org.beangle.commons.bean.meta.MetaModel.ClassMeta
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings.*
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.BeanInfo.*

import java.lang.Character.isUpperCase
import java.lang.invoke.{MethodHandle, MethodHandles}
import java.lang.reflect.{Method, Modifier}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable

/** BeanInfo introspection and PropertyInfo. */
object BeanInfo {

  /** Ignore java object and scala case class methods
   */
  private val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll") ++
    Set("apply", "unApply", "canEqual")
  private val caseIgnores = Set("productArity", "productIterator", "productPrefix", "productElement", "productElementName", "productElementNames", "copy")

  /** Property metadata: holds [[MetaModel.Property]] reference + accessor MethodHandles.
    *
    * Delegates name/typeinfo/isTransient/isOptional to the underlying [[MetaModel.Property]],
    * avoiding data duplication when both ClassMeta and BeanInfo are in memory.
    * For optional properties, typeinfo returns the resolved type (Option[X]) by wrapping
    * the flattened element type stored in MetaModel.Property.
    */
  class PropertyInfo(val meta: MetaModel.Property, val getter: Option[MethodHandle], val setter: Option[MethodHandle]) {
    def name: String = meta.name

    def typeinfo: TypeInfo = if meta.isOptional then TypeInfo.get(classOf[Option[_]], List(meta.typeinfo)) else meta.typeinfo

    def isTransient: Boolean = meta.isTransient

    def isOptional: Boolean = meta.isOptional

    /** Returns true if property has setter. */
    def writable: Boolean = setter.isDefined

    /** Property type class. */
    def clazz: Class[_] = typeinfo.clazz

    /** Returns true if property has getter. */
    def readable: Boolean = getter.isDefined

    override def toString: String = {
      if writable && readable then s"var $name: $typeinfo = _ "
      else if readable then s"def $name: $typeinfo"
      else s"def $name(x1: $typeinfo)"
    }
  }

  /** Constructor or method parameter metadata. */
  case class ParamInfo(name: String, typeinfo: TypeInfo, defaultValue: Option[Any])

  /** Method metadata: holds [[MetaModel.Method]] reference + handler MethodHandle.
    *
    * Delegates name and parameter matching to the underlying [[MetaModel.Method]].
    */
  case class MethodInfo(meta: MetaModel.Method, handler: MethodHandle) {
    def name: String = meta.name

    override def toString: String = s"def $name(${meta.paramTypes.mkString(", ")})"

    /** Returns true if the given args match parameter types. */
    def matches(args: Any*): Boolean = meta.matches(args*)
  }

  /** Constructs a BeanInfo from a [[ClassMeta]] (binary-decoded or compile-time digged).
    *
    * Uses getter/setter names from Property metadata for direct method lookup,
    * avoiding full method scans for each property.
    */
  def from(cm: ClassMeta): BeanInfo = {
    val clazz = cm.clazz

    // Pre-group all methods by name for O(1) lookup
    val methodsByName = clazz.getMethods.groupBy(_.getName)

    /** Finds a method by name, preferring non-bridge. */
    def findByName(name: String): Option[Method] = {
      methodsByName.get(name).flatMap(cs => cs.find(!_.isBridge).orElse(cs.headOption))
    }

    /** Finds a method by name and parameter type signature. */
    def findBySignature(name: String, paramTypes: Seq[TypeInfo]): Option[Method] = {
      methodsByName.getOrElse(name, Array.empty[Method]).find { m =>
        m.getParameterTypes.length == paramTypes.length &&
          m.getParameterTypes.zip(paramTypes).forall((pt, expected) => pt == expected.clazz)
      }
    }

    // Build properties using getter/setter names for direct lookup.
    // For Scala 3 macro-generated ClassMeta, getterName may be None for var fields
    // (var getters are implicit in Scala 3's macro API). Fall back to property name.
    val properties = cm.properties.map { p =>
      // Scala 3 macro may not set getterName for var/val fields (getters are implicit).
      // Always fall back to property name — for both var (has setter) and val (no setter).
      val getterName = p.getterName.orElse(Some(p.name))
      val getter = getterName.flatMap(findByName).map(Builder.unreflect)
      val setter = p.setterName.flatMap(findByName).map(Builder.unreflect)
      (p.name, PropertyInfo(p, getter, setter))
    }.toMap

    // Build methods: find matching method for each cm.methods entry
    val methods = cm.methods.flatMap { m =>
      findBySignature(m.name, m.paramTypes).map { method =>
        (m.name, MethodInfo(m, Builder.unreflect(method)))
      }
    }.groupBy(_._1).map { case (name, entries) =>
      (name, ArraySeq.from(entries.map(_._2)))
    }

    BeanInfo(cm, properties, methods)
  }

  object Builder {
    /** Returns true if property should be treated as transient. */
    def isTransient(transientAnnotated: Boolean, hasSetter: Boolean, usedInPrimaryCtor: Boolean): Boolean = {
      if transientAnnotated then true else !usedInPrimaryCtor && !hasSetter
    }

    /** Returns true if method is a case class intrinsic (productArity, copy, etc.). */
    def isCaseMethod(isCase: Boolean, name: String): Boolean = {
      isCase && caseIgnores.contains(name)
    }

    /** Returns true if method is a candidate for property accessor discovery. */
    def isFineMethod(isCase: Boolean, method: Method, allowBridge: Boolean = false): Boolean = {
      val modifiers = method.getModifiers
      val name = method.getName
      val ignored = BeanInfo.ignores.contains(name) || isCaseMethod(isCase, name)
      val modifierNice = !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
      !method.isAnnotationPresent(classOf[noreflect]) && !ignored && modifierNice && isFineMethodName(name) && (!method.isBridge || allowBridge)
    }

    /** Returns true if method name follows getter/setter convention. */
    def isFineMethodName(name: String): Boolean = {
      if name.startsWith("_") then false
      else if name.endsWith("_$eq") then !name.substring(0, name.length - 4).contains("$")
      else !name.contains("$")
    }

    /** Returns true if method return/param types match the given method info. */
    def isSignatureMatchable(method: Method, methodInfo: (TypeInfo, ArraySeq[ParamInfo])): Boolean = {
      if classOf[AnyRef] != method.getReturnType && !method.getReturnType.isAssignableFrom(methodInfo._1.clazz) then false
      else {
        val ps = method.getParameterTypes
        val types = methodInfo._2
        types.size == ps.length &&
          (0 until ps.length).forall { i => ps(i) == classOf[AnyRef] || ps(i).isAssignableFrom(types(i).typeinfo.clazz) }
      }
    }

    /** Returns (true, propertyName) for getter, (false, propertyName) for setter, or None. */
    def findAccessor(method: Method): Option[Tuple2[Boolean, String]] = {
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
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else if (name.endsWith("_=")) substringBefore(name, "_=")
        else null
      }
    }

    /** Filters out bridge and overridden methods using Method info for comparison. */
    def filterSameNames(methods: Iterable[(Method, MethodInfo)]): collection.Seq[(Method, MethodInfo)] = {
      if (methods.size == 1) {
        methods.toSeq
      } else {
        val result = Collections.newBuffer[(Method, MethodInfo)]
        val paramSizeMap = methods.groupBy(_._1.getParameterCount)
        paramSizeMap.values foreach { ml =>
          val reminded = Collections.newBuffer(ml)
          ml foreach { case (m, mi) =>
            reminded.find { case (om, omi) =>
              isMethodOver(om, m)  // om is preferred over m
            } foreach (_ => reminded -= ((m, mi)))
          }
          result ++= reminded
        }
        result
      }
    }

    /** Returns true if method m is preferred over o (for overload resolution). */
    private def isMethodOver(m: Method, o: Method): Boolean = {
      if m != o && m.getName == o.getName && m.getParameterCount == o.getParameterCount then
        if o.getReturnType == classOf[AnyRef] || o.getReturnType.isAssignableFrom(m.getReturnType) then
          val ps = o.getParameterTypes
          val mps = m.getParameterTypes
          val paramTypeMatch = (0 until mps.length).forall { i => ps(i) == classOf[AnyRef] || ps(i).isAssignableFrom(mps(i)) }
          if paramTypeMatch then
            o.isBridge || o.getDeclaringClass.isAssignableFrom(m.getDeclaringClass)
          else false
        else false
      else false
    }

    private def lower(name: String): String = {
      if (name.length > 1 && isUpperCase(name.charAt(1))) name else uncapitalize(name)
    }

    /** Unreflects a resolved accessor Method into a MethodHandle (setAccessible fallback for non-public classes). */
    private[reflect] def unreflect(m: Method): MethodHandle = {
      try MethodHandles.lookup().unreflect(m)
      catch
        case _: IllegalAccessException =>
          m.setAccessible(true)
          MethodHandles.lookup().unreflect(m)
    }
  }
}

/** Introspection info for a Java/Scala class.
  *
  * Holds a [[ClassMeta]] reference as the single source of truth for class metadata.
  * Properties and methods add accessor MethodHandles on top of the metadata.
  */
class BeanInfo(val meta: ClassMeta, val properties: Map[String, PropertyInfo],
               val methods: Map[String, ArraySeq[MethodInfo]]) {

  def clazz: Class[_] = meta.clazz

  def ctors: Seq[MetaModel.Ctor] = meta.ctors

  override def toString: String = {
    val sb = new mutable.ArrayBuffer[String]
    val isCase = TypeInfo.isCaseClass(clazz)
    val fieldInCtor = if ctors.isEmpty then Set.empty else ctors.head.parameters.map(_.name).toSet
    if (ctors.isEmpty) {
      sb += s"class ${clazz.getName} {"
    } else {
      val ctorStr = ctors.head.parameters.map(p =>
        p.name + ": " + p.typeinfo + (if p.defaultValue.nonEmpty then " = " + p.defaultValue.get.toString else "")
      ).mkString(", ")
      sb += s"${if isCase then "case " else ""}class ${clazz.getName}($ctorStr) {"
      ctors.tail foreach { ctor =>
        val params = ctor.parameters.map(p =>
          p.name + ": " + p.typeinfo + (if p.defaultValue.nonEmpty then " = " + p.defaultValue.get.toString else "")
        ).mkString(", ")
        sb += s"  def this($params)"
      }
    }

    properties foreach { (name, pi) =>
      if (pi.setter.nonEmpty || !fieldInCtor.contains(name)) {
        sb += s"  ${pi}"
      }
    }
    sb += "}"
    sb.mkString("\n")
  }

  /** Gets TypeInfo for property. */
  def getPropertyTypeInfo(property: String): Option[TypeInfo] = {
    properties.get(property) match {
      case Some(p) => Some(p.typeinfo)
      case None => None
    }
  }

  /** Gets property type class. */
  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property).map(_.clazz)
  }

  /** Gets getter MethodHandle for property (invocation layer). */
  def getGetter(property: String): Option[MethodHandle] = {
    properties.get(property) match {
      case Some(p) => p.getter
      case None => None
    }
  }

  /** Gets setter MethodHandle for property (invocation layer). */
  def getSetter(property: String): Option[MethodHandle] = {
    properties.get(property) match {
      case Some(p) => p.setter
      case None => None
    }
  }

  /** Properties with getters. */
  def readables: Map[String, PropertyInfo] = properties.filter(x => x._2.readable)

  /** Properties with setters. */
  def writables: Map[String, PropertyInfo] = properties.filter(x => x._2.writable)
}
