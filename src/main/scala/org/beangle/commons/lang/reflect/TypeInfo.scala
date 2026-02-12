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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings.{capitalize, replace}

import java.lang.reflect.{Constructor, Method, ParameterizedType}
import scala.collection.immutable.ArraySeq
import scala.collection.{immutable, mutable}
import scala.language.existentials

/** Type metadata (class, generic args). */
sealed trait TypeInfo {
  /** Full type name including generic args. */
  def name: String = TypeInfo.typeName(clazz, args)

  /** The class. */
  def clazz: Class[_]

  /** Type arguments (empty for non-generic types). */
  def args: ArraySeq[TypeInfo]

  /** True if this is Option[T]. */
  final def isOptional: Boolean = clazz == classOf[Option[_]]

  /** True if this is Iterable or Map. */
  final def isIterable: Boolean = TypeInfo.isIterableType(clazz)

  override def toString: String = name
}

/** TypeInfo utilities (collection/map detection, type name). */
object TypeInfo {

  /** Returns true if the class is a Collection (Iterable, util.Collection, or Array). */
  def isCollectionType(clazz: Class[_]): Boolean = {
    !isMapType(clazz) && classOf[collection.Iterable[_]].isAssignableFrom(clazz) ||
      classOf[java.util.Collection[_]].isAssignableFrom(clazz) || clazz.isArray
  }

  /** Returns true if the class is Iterable or Map. */
  def isIterableType(clazz: Class[_]): Boolean = {
    isMapType(clazz) || isCollectionType(clazz)
  }

  /** Returns true if the class is a Map. */
  def isMapType(clazz: Class[_]): Boolean = {
    classOf[collection.Map[_, _]].isAssignableFrom(clazz) || classOf[java.util.Map[_, _]].isAssignableFrom(clazz)
  }

  /** Returns a short Scala-style type name for the class. */
  def scalaTypeName(clazz: Class[_]): String = {
    if (clazz.isPrimitive) {
      if clazz == classOf[Unit] then "Unit" else capitalize(clazz.getName)
    } else {
      if clazz == classOf[String] then "String"
      else if clazz == classOf[Option[_]] then "Option"
      else if clazz.isArray then "Array"
      else if clazz == classOf[AnyRef] then "Object"
      else replace(clazz.getName, "scala.collection.immutable.", "")
    }
  }

  /** Returns full type name including generic args (e.g. List[Int]). */
  def typeName(clazz: Class[_], args: collection.Seq[TypeInfo]): String = {
    if args.isEmpty then scalaTypeName(clazz)
    else scalaTypeName(clazz) + args.map(_.name).mkString("[", ",", "]")
  }

  /** Returns true if the class is a Scala case class (Product but not Tuple). */
  def isCaseClass(clazz: Class[_]): Boolean = {
    classOf[Product].isAssignableFrom(clazz) && !clazz.getName.startsWith("Tuple")
  }

  /** TypeInfo for AnyRef. */
  val AnyRefType = GeneralType(classOf[AnyRef])
  /** TypeInfo for Unit. */
  val UnitType = GeneralType(classOf[Unit])

  /** Cache for created TypeInfo instances. */
  var cache: Map[String, TypeInfo] = Map.empty

  cache += (AnyRefType.name, AnyRefType)
  cache += (UnitType.name, UnitType)

  /** Gets TypeInfo for a class (non-optional). */
  def get(clazz: Class[_]): TypeInfo = {
    get(clazz, false)
  }

  /** Gets TypeInfo for a class, optionally wrapped in Option.
   *
   * @param clazz    the class
   * @param optional if true, wraps result in Option
   * @return TypeInfo
   */
  def get(clazz: Class[_], optional: Boolean): TypeInfo = {
    val args: ArraySeq[TypeInfo] =
      if clazz.isArray then ArraySeq(get(clazz.getComponentType, false))
      else if isCollectionType(clazz) then Reflections.getCollectionParamTypes(clazz)
      else if isMapType(clazz) then Reflections.getMapParamTypes(clazz)
      else ArraySeq.empty

    val typeinfo = get(clazz, args)
    if (optional) get(classOf[Option[_]], List(typeinfo)) else typeinfo
  }

  /** Gets TypeInfo with type arguments (Class[_]*). */
  def get(clazz: Class[_], first: Class[_], tails: Class[_]*): TypeInfo = {
    get(clazz, GeneralType(first) :: tails.map(GeneralType(_)).toList)
  }

  /** Gets TypeInfo with type arguments (Array). */
  def get(clazz: Class[_], args: Array[TypeInfo]): TypeInfo = {
    get(clazz, ArraySeq.from(args))
  }

  /** Gets or creates TypeInfo for class with args; uses cache. */
  def get(clazz: Class[_], args: collection.Seq[TypeInfo]): TypeInfo = {
    val name = typeName(clazz, args)
    cache.get(name) match {
      case Some(ti) => ti
      case None =>
        val typeArgs = ArraySeq.from(args)
        val newInfo =
          if clazz == classOf[Option[_]] then OptionType(args.head)
          else if isIterableType(clazz) then IterableType(clazz, typeArgs)
          else GeneralType(clazz, ArraySeq.from(args))
        cache += (name, newInfo)
        newInfo
    }
  }

  /** Converts object (Class or Array) to string representation. */
  def stringz(obj: Any): String = {
    obj match {
      case clz: Class[_] => clz.toString
      case d: Array[_] =>
        d.map(x => stringz(x)).mkString("[", ",", "]")
    }
  }

  /** Converts object (Class or [Class, Class[]]) to TypeInfo. */
  def convert(obj: Any): TypeInfo = {
    obj match {
      case clz: Class[_] => TypeInfo.get(clz, false)
      case a: Array[Any] =>
        val clz = a(0)
        val argsClz = a(1).asInstanceOf[Array[_]]
        val argsInfo = Array.ofDim[TypeInfo](argsClz.length)
        argsClz.indices foreach { i =>
          argsInfo(i) = convert(argsClz(i))
        }
        TypeInfo.get(clz.asInstanceOf[Class[_]], argsInfo)
    }
  }

  /** General type (class + optional type args). */
  case class GeneralType(clazz: Class[_], args: ArraySeq[TypeInfo] = ArraySeq.empty) extends TypeInfo

  /** Option[T] type info. */
  case class OptionType(elementType: TypeInfo) extends TypeInfo {
    def clazz = classOf[Option[_]]

    def args = ArraySeq(elementType)
  }

  /** Iterable/Map type info with element types. */
  case class IterableType(clazz: Class[_], args: ArraySeq[TypeInfo]) extends TypeInfo {
    /** Returns true if this is a Set type. */
    def isSet: Boolean = {
      classOf[collection.Set[_]].isAssignableFrom(clazz) || classOf[java.util.Set[_]].isAssignableFrom(clazz)
    }

    /** Returns true if this is a Collection type. */
    def isCollection: Boolean = isCollectionType(clazz)

    /** Returns true if this is a Map type. */
    def isMap: Boolean = isMapType(clazz)

    /** Returns element type (for Collection) or Tuple2 (for Map). */
    def elementType: TypeInfo = {
      if isMap then GeneralType(classOf[Tuple2[_, _]], args) else args.head
    }
  }
}
