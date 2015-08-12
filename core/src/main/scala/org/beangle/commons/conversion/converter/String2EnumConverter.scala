/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.ClassLoaders

/**
 * Convert String to Enumeration.
 *
 * @author chaostone
 * @since 3.2.0
 */
class String2EnumConverter extends StringConverterFactory[String, Enum[_]] {

  private class EnumConverter[T <: Enum[_]](val enumType: Class[_]) extends Converter[String, T] {
    def enumValueOf[T <: Enum[T]](cls: Class[_], str: String): T = {
      Enum.valueOf(cls.asInstanceOf[Class[T]], str)
    }

    override def apply(input: String): T = enumValueOf(enumType, input)
  }

  override def getConverter[T <: Enum[_]](targetType: Class[T]): Option[Converter[String, T]] = {
    val converter = super.getConverter(targetType)
    if (converter.isEmpty) {
      val enumconverter = new EnumConverter(targetType)
      register(targetType, enumconverter)
      Some(enumconverter)
    } else {
      converter
    }
  }

}

object String2ScalaEnumConverter extends StringConverterFactory[String, Enumeration#Value] {

  private class EnumConverter[T <: Enumeration#Value](enum: Enumeration) extends Converter[String, T] {
    override def apply(input: String): T = {
      val result =
        if (Numbers.isDigits(input)) enum.apply(Numbers.toInt(input))
        else enum.withName(input)
      result.asInstanceOf[T]
    }
  }

  override def getConverter[T <: Enumeration#Value](targetType: Class[T]): Option[Converter[String, T]] = {
    val converter = super.getConverter(targetType)
    if (converter.isEmpty) {
      val targetTypeName = targetType.getName()
      val dollaIdx = targetTypeName.indexOf('$')
      if (-1 == dollaIdx) throw new RuntimeException("Only support enum which value extends super.Val")
      val enum = ClassLoaders.load(targetTypeName.substring(0, dollaIdx + 1)).getDeclaredField("MODULE$").get(null).asInstanceOf[Enumeration]
      val enumconverter = new EnumConverter(enum)
      register(targetType, enumconverter)
      Some(enumconverter)
    } else {
      converter
    }
  }
}