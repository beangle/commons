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

import org.beangle.commons.lang.reflect.TypeInfo
import org.beangle.commons.lang.reflect.TypeInfo.OptionType

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

/** Pure data model for a single class's metadata (properties / ctors / methods).
  *
  * Produced by [[MetaCodec.parse]] (binary decode) or [[MetaLoader.load]] (reflection).
  * Use [[org.beangle.commons.lang.reflect.BeanInfo.from]] to reconstruct BeanInfo
  * with accessor MethodHandles.
  */
object MetaModel {

  /** Class metadata: properties, constructors, and methods. */
  case class ClassMeta(clazz: Class[_], properties: Seq[Property], ctors: Seq[Ctor], methods: Seq[Method])

  /** Property declaration: name, type, flags, and optional accessor method names.
    *
    * Option properties: typeinfo stores the **element type** (flattened), isOptional=true;
    * reconstruct as `if isOptional then OptionType(typeinfo) else typeinfo`.
    */
  case class Property(
    name: String,
    typeinfo: TypeInfo,
    isTransient: Boolean,
    isOptional: Boolean,
    getterName: Option[String] = None,
    setterName: Option[String] = None
  )

  /** Constructor declaration. */
  case class Ctor(parameters: Seq[Param])

  /** Constructor parameter declaration. */
  case class Param(name: String, typeinfo: TypeInfo, defaultValue: Option[Any])

  /** Method declaration: name + parameter types (TypeInfo preserves generic precision). */
  case class Method(name: String, paramTypes: Seq[TypeInfo]) {

    /** Returns true if the given args match parameter types. */
    def matches(args: Any*): Boolean = {
      if (paramTypes.length != args.length) return false
      !args.indices.exists { i =>
        null != args(i) && !paramTypes(i).clazz.isInstance(args(i))
      }
    }
  }

  /** Constructor parameter holder for [[Builder.addCtor]]. */
  class ParamHolder(name: String, typeinfo: Any, defaultValue: Option[Any]) {
    def this(name: String, typeInfo: Any) = this(name, typeInfo, None)

    /** Converts to Param. */
    def toParam: Param = Param(name, TypeInfo.convert(typeinfo), defaultValue)
  }

  /** ClassMeta builder — driven by compile-time digger [[MetaDigger]].
    *
    * The macro resolves fields, constructors, and methods at compile time;
    * runtime only performs type conversion and assembly.
    * Properties are sorted by name, methods by (name, params), ensuring deterministic binary output.
    */
  class Builder(val clazz: Class[_]) {
    private val properties = new mutable.ArrayBuffer[Property]
    private val ctors = new mutable.ArrayBuffer[Ctor]
    private val methods = new mutable.ArrayBuffer[Method]

    /** Adds a property; Option type is peeled to its element type with isOptional=true
      * (same shape as [[MetaModels.of]]).
      */
    def addProperty(name: String, ti: Any, isTransient: Boolean,
                    getterName: Option[String] = None, setterName: Option[String] = None): Unit = {
      TypeInfo.convert(ti) match
        case o: OptionType => properties += Property(name, o.elementType, isTransient, isOptional = true, getterName, setterName)
        case other         => properties += Property(name, other, isTransient, isOptional = false, getterName, setterName)
    }

    /** Adds a constructor with parameters (head of `build()` result is the primary one). */
    def addCtor(paramInfos: Array[ParamHolder]): Unit =
      ctors += Ctor(paramInfos.toSeq.map(_.toParam))

    /** Adds a method record with parameter type infos (Any for macro compatibility). */
    def addMethod(name: String, paramTypes: Array[Any]): Unit =
      methods += Method(name, ArraySeq.from(paramTypes.map(TypeInfo.convert)))

    /** Assembles the ClassMeta (properties sorted by name, methods by name+params). */
    def build(): ClassMeta =
      ClassMeta(clazz, properties.toSeq.sortBy(_.name), ctors.toSeq,
        methods.toSeq.sortBy(m => (m.name, m.paramTypes.map(_.clazz.getName).mkString("|"))))
  }
}
