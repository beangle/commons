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

package org.beangle.commons.conversion.string

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.lang.{ClassLoaders, Enums}

import java.lang.reflect.Method
import scala.reflect.Enum as ScalaEnum

/**
  * Convert String to Enumeration.
  *
  * @author chaostone
  * @since 3.2.0
  */
object EnumConverters extends StringConverterFactory[String, ScalaEnum] {

  class EnumConverter[T](e: AnyRef) extends Converter[String, T] {
    private val values = findValues(e)
    override def apply(input: String): T = values(input).asInstanceOf[T]
  }

  override def getConverter[T](targetType: Class[T]): Option[Converter[String, T]] = {
    val converter = super.getConverter(targetType)
    if converter.isEmpty then
      val enumconverter = newConverter(targetType).asInstanceOf[Converter[String, T]]
      register(targetType, enumconverter)
      Some(enumconverter)
    else converter
  }

  private def newConverter(targetType: Class[_]): EnumConverter[AnyRef] = {
    val enm = Reflections.getInstance[AnyRef](targetType.getName)
    new EnumConverter(enm)
  }

  private def findValues(e: AnyRef): Map[String, AnyRef] = {
    val values = Enums.values(e)
    val idValues = Enums.toIdMaps(values).map(x => (x._1.toString, x._2))
    val nameValues = values.map { v => (v.toString, v) }
    idValues ++ nameValues
  }
}

object JavaEnumConverters extends StringConverterFactory[String, Enum[_]] {

  private class EnumConverter[T <: Enum[_]](val enumType: Class[_]) extends Converter[String, T] {

    def enumValueOf[T <: Enum[T]](cls: Class[_], str: String): T = Enum.valueOf(cls.asInstanceOf[Class[T]], str)

    override def apply(input: String): T = enumValueOf(enumType, input)
  }

  override def getConverter[T](targetType: Class[T]): Option[Converter[String, T]] = {
    val converter = super.getConverter(targetType)
    if converter.isEmpty then
      val enumconverter = new EnumConverter(targetType)
      register(targetType, enumconverter)
      Some(enumconverter)
    else converter
  }
}
