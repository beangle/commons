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

import org.beangle.commons.json.JsonObject
import org.beangle.commons.lang.reflect.TypeInfo
import org.beangle.commons.lang.reflect.TypeInfo.OptionType

import scala.collection.mutable

/** Pure data model for a single class's metadata (properties / ctors).
  *
  * Produced by [[MetaCodec.parse]] (binary decode) or [[MetaLoader.load]] (reflection).
  * Use [[org.beangle.commons.lang.reflect.BeanInfo.from]] to reconstruct BeanInfo
  * with accessor MethodHandles.
  */
object MetaModel {

  /** Bean metadata: properties and constructors. */
  case class BeanMeta(clazz: Class[_], properties: Seq[Property], ctors: Seq[Ctor]) {
    /** Renders as human-readable JSON. */
    override def toString: String = JsonObject(
      "clazz" -> clazz.getName,
      "properties" -> properties.map(p => JsonObject(
        "name" -> p.name,
        "type" -> p.typeinfo.name,
        "transient" -> p.isTransient,
        "optional" -> p.isOptional)),
      "ctors" -> ctors.map(c => JsonObject(
        "parameters" -> c.parameters.map(pm => JsonObject(
          "name" -> pm.name,
          "type" -> pm.typeinfo.name,
          "default" -> pm.defaultValue.map(v => if null == v then null else v.toString)))))
    ).toJson
  }

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

  /** Constructor parameter holder for [[Builder.addCtor]]. */
  class ParamHolder(name: String, typeinfo: Any, defaultValue: Option[Any]) {
    def this(name: String, typeInfo: Any) = this(name, typeInfo, None)

    /** Converts to Param. */
    def toParam: Param = Param(name, TypeInfo.convert(typeinfo), defaultValue)
  }

  /** BeanMeta builder — driven by compile-time digger [[MetaDigger]].
    *
    * The macro resolves fields and constructors at compile time;
    * runtime only performs type conversion and assembly.
    * Properties are sorted by name, ensuring deterministic binary output.
    */
  class Builder(val clazz: Class[_]) {
    private val properties = new mutable.ArrayBuffer[Property]
    private val ctors = new mutable.ArrayBuffer[Ctor]

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

    /** Assembles the BeanMeta (properties sorted by name). */
    def build(): BeanMeta =
      BeanMeta(clazz, properties.toSeq.sortBy(_.name), ctors.toSeq)
  }
}
