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

import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.bean.meta.{MetaLoader, MetaModel}
import org.beangle.commons.lang.reflect.BeanInfo.*

import java.lang.invoke.{MethodHandle, MethodHandles}
import java.lang.reflect.{Method, Modifier}
import scala.collection.mutable

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
    val methodsByName: Map[String, Seq[Method]] = clazz.getMethods.groupBy(_.getName).view.mapValues(_.toSeq).toMap

    /** Finds a method by name, preferring non-bridge. */
    def findByName(name: String): Option[Method] = {
      methodsByName.get(name).flatMap(cs => cs.find(!_.isBridge).orElse(cs.headOption))
    }

    // Build properties using getter/setter names from BeanMeta for direct lookup.
    // Only properties with a resolved getter are included.
    val lookup = MethodHandles.lookup()
    val properties = cm.properties.flatMap { p =>
      findByName(p.getterName).map { getterMethod =>
        val setter = p.setterName.flatMap(findByName).map(m => Invokers.unreflect(lookup, m))
        (p.name, PropertyInfo(p, Invokers.unreflect(lookup, getterMethod), setter))
      }
    }.toMap

    val (missing, writeOnlys) = discoverMissing(methodsByName, properties, lookup)
    BeanInfo(cm, properties ++ missing, writeOnlys)
  }

  /** 遍历方法表补齐 BeanMeta 缺失的 JavaBean 属性（如 Scala 类继承自 Java 父类的
   * getter/setter，编译期 dig 只含自身成员）：有 getter 的并入可读属性，仅 setter 的归入 writeOnlys。
   */
  private def discoverMissing(methodsByName: Map[String, Seq[Method]], existing: Map[String, PropertyInfo],
                              lookup: MethodHandles.Lookup): (Map[String, PropertyInfo], Map[String, Method]) = {
    val getters = new mutable.HashMap[String, Method]
    val setters = new mutable.HashMap[String, Method]
    methodsByName.values.flatten.foreach { m =>
      if !Modifier.isStatic(m.getModifiers) && Modifier.isPublic(m.getModifiers) then
        if m.getParameterCount == 0 && m.getReturnType != classOf[Unit] then
          val name = MetaLoader.getPropertyName(m.getName, true)
          if name != m.getName && !BeanInfo.ignoredNames.contains(m.getName) then getters.put(name, m)
        else if m.getParameterCount == 1 then
          val name = MetaLoader.getPropertyName(m.getName, false)
          if null != name && !name.contains("$") then setters.put(name, m)
    }
    (getters.keySet ++ setters.keySet).foldLeft((Map.empty[String, PropertyInfo], Map.empty[String, Method])) {
      case (acc, name) if !existing.contains(name) =>
        getters.get(name) match
          case Some(g) =>
            val setter = setters.get(name)
            val meta = MetaModel.Property(name, TypeInfo.get(g.getReturnType), isTransient = false, isOptional = false,
              g.getName, setter.map(_.getName))
            val pi = PropertyInfo(meta, Invokers.unreflect(lookup, g), setter.map(m => Invokers.unreflect(lookup, m)))
            (acc._1 + (name -> pi), acc._2)
          case None =>
            if setters.contains(name) && !hasGetter(methodsByName, name) then (acc._1, acc._2 + (name -> setters(name)))
            else acc
      case (acc, _) => acc
    }
  }

  private def hasGetter(methodsByName: Map[String, Seq[Method]], name: String): Boolean = {
    methodsByName.values.flatten.exists { m =>
      m.getParameterCount == 0 && m.getReturnType != classOf[Unit] &&
        (m.getName == name || MetaLoader.getPropertyName(m.getName, true) == name)
    }
  }
}

/** Introspection info for a Java/Scala class.
 *
 * Holds a [[BeanMeta]] reference as the single source of truth for class metadata.
 * Properties add accessor MethodHandles on top of the metadata.
 */
class BeanInfo(val meta: BeanMeta, val properties: Map[String, PropertyInfo], val writeOnlys: Map[String, Method] = Map.empty) {

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

  /** Gets the setter Method for a property by name; write-only 属性回退到 writeOnlys。 */
  def getSetterMethod(property: String): Option[Method] = {
    properties.get(property).flatMap { p =>
      p.meta.setterName.flatMap { setterName =>
        try Some(clazz.getMethod(setterName, p.typeinfo.clazz))
        catch case _: NoSuchMethodException => None
      }
    }.orElse(writeOnlys.get(property))
  }

  /** 零参方法查找：`Class.getMethod(name)` 只匹配无参方法，适用于 getter。 */
  private def findMethod(name: String): Option[Method] = {
    try Some(clazz.getMethod(name)) catch case _: NoSuchMethodException => None
  }

  /** Properties with setters. */
  def writables: Map[String, PropertyInfo] = properties.filter(x => x._2.writable)
}
