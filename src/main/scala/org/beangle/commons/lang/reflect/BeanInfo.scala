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
import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.lang.reflect.BeanInfo.*

import java.lang.invoke.{MethodHandle, MethodHandles}
import java.lang.reflect.{Method, Modifier}

/** BeanInfo introspection and PropertyInfo. */
object BeanInfo {

  private val ignoredNames = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify",
    "notifyAll", "apply", "unApply", "canEqual", "productArity", "productIterator", "productPrefix",
    "productElement", "productElementName", "productElementNames", "copy")

  private def isFineMethodName(name: String): Boolean = {
    if name.startsWith("_") then false
    else if name.endsWith("_$eq") then !name.substring(0, name.length - 4).contains("$")
    else !name.contains("$")
  }

  /** Property metadata: holds [[MetaModel.Property]] reference + accessor MethodHandles.
    *
    * Delegates name/typeinfo/isTransient/isOptional to the underlying [[MetaModel.Property]],
    * avoiding data duplication when both BeanMeta and BeanInfo are in memory.
    * For optional properties, typeinfo returns the resolved type (Option[X]) by wrapping
    * the flattened element type stored in MetaModel.Property.
    */
  class PropertyInfo(val meta: MetaModel.Property, val getter: MethodHandle, val setter: Option[MethodHandle]) {
    def name: String = meta.name

    def typeinfo: TypeInfo = if meta.isOptional then TypeInfo.get(classOf[Option[_]], List(meta.typeinfo)) else meta.typeinfo

    def isTransient: Boolean = meta.isTransient

    def isOptional: Boolean = meta.isOptional

    /** Returns true if property has setter. */
    def writable: Boolean = setter.isDefined

    /** Property type class. */
    def clazz: Class[_] = typeinfo.clazz

    override def toString: String = {
      if writable then s"var $name: $typeinfo = _ "
      else s"def $name: $typeinfo"
    }
  }

  /** Constructs a BeanInfo from a [[BeanMeta]] (binary-decoded or compile-time digged).
    *
    * Uses getter/setter names from Property metadata for direct method lookup,
    * avoiding full method scans for each property.
    */
  def from(cm: BeanMeta): BeanInfo = {
    val clazz = cm.clazz

    // Pre-group all methods by name for O(1) lookup
    val methodsByName = clazz.getMethods.groupBy(_.getName)

    /** Finds a method by name, preferring non-bridge. */
    def findByName(name: String): Option[Method] = {
      methodsByName.get(name).flatMap(cs => cs.find(!_.isBridge).orElse(cs.headOption))
    }

    // Build properties using getter/setter names from BeanMeta for direct lookup.
    // Only properties with a resolved getter are included.
    val properties = cm.properties.flatMap { p =>
      findByName(p.getterName).map { getterMethod =>
        val setter = p.setterName.flatMap(findByName).map(unreflect)
        (p.name, PropertyInfo(p, unreflect(getterMethod), setter))
      }
    }.toMap

    BeanInfo(cm, properties)
  }

  /** Unreflects a Method into a MethodHandle (setAccessible fallback for non-public classes). */
  private def unreflect(m: Method): MethodHandle = {
    try MethodHandles.lookup().unreflect(m)
    catch
      case _: IllegalAccessException =>
        m.setAccessible(true)
        MethodHandles.lookup().unreflect(m)
  }
}

/** Introspection info for a Java/Scala class.
  *
  * Holds a [[BeanMeta]] reference as the single source of truth for class metadata.
  * Properties add accessor MethodHandles on top of the metadata.
  */
class BeanInfo(val meta: BeanMeta, val properties: Map[String, PropertyInfo]) {

  def clazz: Class[_] = meta.clazz

  def ctors: Seq[MetaModel.Ctor] = meta.ctors

  /** 按方法名分组的实例方法（public、非 static、排除属性 accessor 与 object/case 内置方法）。
    *
    * 惰性计算：仅在使用方需要方法表时反射 `clazz.getMethods`（native 下依赖
    * allPublicMethods 注册，见 AotPlugin 生成物）。
    */
  lazy val methods: Map[String, Seq[Method]] = {
    val accessorNames = meta.properties.flatMap(p => p.getterName :: p.setterName.toList).toSet
    clazz.getMethods.toSeq
      .filter(m => !Modifier.isStatic(m.getModifiers) &&
        !BeanInfo.ignoredNames.contains(m.getName) && BeanInfo.isFineMethodName(m.getName) &&
        !accessorNames.contains(m.getName))
      .groupBy(_.getName)
  }

  override def toString: String = meta.toString

  /** Gets TypeInfo for property. */
  def getPropertyTypeInfo(property: String): Option[TypeInfo] = {
    properties.get(property).map(_.typeinfo)
  }

  /** Gets property type class. */
  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property).map(_.clazz)
  }

  /** Gets getter MethodHandle for property. */
  def getGetter(property: String): Option[MethodHandle] = {
    properties.get(property).map(_.getter)
  }

  /** Gets setter MethodHandle for property. */
  def getSetter(property: String): Option[MethodHandle] = {
    properties.get(property).flatMap(_.setter)
  }

  /** Gets the getter Method for a property by name.
    * Uses Class.getMethod which returns the most specific (non-bridge) overload.
    */
  def getGetterMethod(property: String): Option[Method] = {
    properties.get(property).flatMap(p => findMethod(p.meta.getterName))
  }

  /** Gets the setter Method for a property by name. */
  def getSetterMethod(property: String): Option[Method] = {
    properties.get(property).flatMap { p =>
      p.meta.setterName.flatMap { setterName =>
        try Some(clazz.getMethod(setterName, p.typeinfo.clazz))
        catch case _: NoSuchMethodException => None
      }
    }
  }

  /** 零参方法查找：`Class.getMethod(name)` 只匹配无参方法，适用于 getter。 */
  private def findMethod(name: String): Option[Method] = {
    try Some(clazz.getMethod(name)) catch case _: NoSuchMethodException => None
  }

  /** Properties with setters. */
  def writables: Map[String, PropertyInfo] = properties.filter(x => x._2.writable)
}
