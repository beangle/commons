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

import org.beangle.commons.lang.reflect.BeanInfo.*

import java.lang.invoke.{MethodHandle, MethodHandles}
import java.lang.reflect.Method
import scala.collection.immutable.ArraySeq

/** BeanInfo introspection and PropertyInfo. */
object BeanInfo {

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

    // Build properties using getter/setter names from ClassMeta for direct lookup.
    val properties = cm.properties.map { p =>
      val getter = p.getterName.flatMap(findByName).map(unreflect)
      val setter = p.setterName.flatMap(findByName).map(unreflect)
      (p.name, PropertyInfo(p, getter, setter))
    }.toMap

    // Build methods: find matching method for each cm.methods entry
    val methods = cm.methods.flatMap { m =>
      findBySignature(m.name, m.paramTypes).map { method =>
        (m.name, MethodInfo(m, unreflect(method)))
      }
    }.groupBy(_._1).map { case (name, entries) =>
      (name, ArraySeq.from(entries.map(_._2)))
    }

    BeanInfo(cm, properties, methods)
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
  * Holds a [[ClassMeta]] reference as the single source of truth for class metadata.
  * Properties and methods add accessor MethodHandles on top of the metadata.
  */
class BeanInfo(val meta: ClassMeta, val properties: Map[String, PropertyInfo],
               val methods: Map[String, ArraySeq[MethodInfo]]) {

  def clazz: Class[_] = meta.clazz

  def ctors: Seq[MetaModel.Ctor] = meta.ctors

  override def toString: String = meta.toString

  /** Gets TypeInfo for property. */
  def getPropertyTypeInfo(property: String): Option[TypeInfo] = {
    properties.get(property).map(_.typeinfo)
  }

  /** Gets property type class. */
  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property).map(_.clazz)
  }

  /** Gets getter MethodHandle for property (invocation layer). */
  def getGetter(property: String): Option[MethodHandle] = {
    properties.get(property).flatMap(_.getter)
  }

  /** Gets setter MethodHandle for property (invocation layer). */
  def getSetter(property: String): Option[MethodHandle] = {
    properties.get(property).flatMap(_.setter)
  }

  /** Properties with getters. */
  def readables: Map[String, PropertyInfo] = properties.filter(x => x._2.readable)

  /** Properties with setters. */
  def writables: Map[String, PropertyInfo] = properties.filter(x => x._2.writable)
}
