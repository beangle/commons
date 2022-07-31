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

sealed trait TypeInfo {
  def name: String = TypeInfo.typeName(clazz, args)

  def clazz: Class[_]

  def args: ArraySeq[TypeInfo]

  final def isOptional: Boolean = clazz == classOf[Option[_]]

  final def isIterable: Boolean = TypeInfo.isIterableType(clazz)

  override def toString: String = name
}

object TypeInfo {

  def isCollectionType(clazz: Class[_]): Boolean = {
    !isMapType(clazz) && classOf[collection.Iterable[_]].isAssignableFrom(clazz) ||
      classOf[java.util.Collection[_]].isAssignableFrom(clazz) || clazz.isArray
  }

  def isIterableType(clazz: Class[_]): Boolean = {
    isMapType(clazz) || isCollectionType(clazz)
  }

  def isMapType(clazz: Class[_]): Boolean = {
    classOf[collection.Map[_, _]].isAssignableFrom(clazz) || classOf[java.util.Map[_, _]].isAssignableFrom(clazz)
  }

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

  def typeName(clazz: Class[_], args: collection.Seq[TypeInfo]): String = {
    if args.isEmpty then scalaTypeName(clazz)
    else scalaTypeName(clazz) + args.map(_.name).mkString("[", ",", "]")
  }

  def isCaseClass(clazz:Class[_]):Boolean={
    classOf[Product].isAssignableFrom(clazz) && !clazz.getName.startsWith("Tuple")
  }

  val AnyRefType = GeneralType(classOf[AnyRef])
  val UnitType = GeneralType(classOf[Unit])

  var cache: Map[String, TypeInfo] = Map.empty

  cache += (AnyRefType.name, AnyRefType)
  cache += (UnitType.name, UnitType)

  def get(clazz: Class[_]): TypeInfo = {
    get(clazz, false)
  }

  def get(clazz: Class[_], optional: Boolean): TypeInfo = {
    val args: ArraySeq[TypeInfo] =
      if clazz.isArray then ArraySeq(get(clazz.getComponentType, false))
      else if isCollectionType(clazz) then Reflections.getCollectionParamTypes(clazz)
      else if isMapType(clazz) then Reflections.getMapParamTypes(clazz)
      else ArraySeq.empty

    val typeinfo = get(clazz, args)
    if (optional) get(classOf[Option[_]], List(typeinfo)) else typeinfo
  }

  def get(clazz: Class[_], first: Class[_], tails: Class[_]*): TypeInfo = {
    get(clazz, GeneralType(first) :: tails.map(GeneralType(_)).toList)
  }

  def get(clazz: Class[_], args:Array[TypeInfo]): TypeInfo = {
    get(clazz,ArraySeq.from(args))
  }

  def get(clazz: Class[_], args:collection.Seq[TypeInfo]): TypeInfo = {
    val name = typeName(clazz, args)
    cache.get(name) match {
      case Some(ti) => ti
      case None =>
        val typeArgs=ArraySeq.from(args)
        val newInfo =
          if clazz == classOf[Option[_]] then OptionType(args.head)
          else if isIterableType(clazz) then IterableType(clazz,typeArgs )
          else GeneralType(clazz, ArraySeq.from(args))
        cache += (name, newInfo)
        newInfo
    }
  }

  def stringz(obj:Any):String={
    obj match{
      case clz:Class[_] => clz.toString
      case d:Array[_]=>
        d.map(x=> stringz(x)).mkString("[",",","]")
    }
  }

  def convert(obj:Any):TypeInfo={
    obj match{
      case clz:Class[_]=> TypeInfo.get(clz,false)
      case a:Array[Any]=>
        val clz = a(0)

        val argsClz = a(1).asInstanceOf[Array[_]]
        val argsInfo = Array.ofDim[TypeInfo](argsClz.length)
        (0 until argsClz.length) foreach{i=>
          argsInfo(i) = convert(argsClz(i))
        }
        TypeInfo.get(clz.asInstanceOf[Class[_]],argsInfo)
    }
  }

  case class GeneralType(clazz: Class[_], args: ArraySeq[TypeInfo] = ArraySeq.empty) extends TypeInfo

  case class OptionType(elementType: TypeInfo) extends TypeInfo {
    def clazz = classOf[Option[_]]

    def args = ArraySeq(elementType)
  }

  case class IterableType(clazz: Class[_], args: ArraySeq[TypeInfo]) extends TypeInfo {
    def isSet: Boolean = {
      classOf[collection.Set[_]].isAssignableFrom(clazz) || classOf[java.util.Set[_]].isAssignableFrom(clazz)
    }

    def isCollection: Boolean = isCollectionType(clazz)

    def isMap: Boolean = isMapType(clazz)

    def elementType: TypeInfo = {
      if isMap then GeneralType(classOf[Tuple2[_, _]], args) else args.head
    }
  }
}
