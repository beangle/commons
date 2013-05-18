/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang.conversion.converter

import java.math.BigDecimal
import java.math.BigInteger

import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.conversion.Converter

/**
 * Convert string to number.
 *
 * @author chaostone
 * @since 3.2.0
 */
class String2NumberConverter extends StringConverterFactory[String, Number] {

  register(classOf[java.lang.Short], new ShortConverter())

  register(classOf[java.lang.Integer], new IntConverter())

  register(classOf[java.lang.Long], new LongConverter())

  register(classOf[java.lang.Float], new FloatConverter())

  register(classOf[java.lang.Double], new DoubleConverter())

  register(classOf[BigInteger], new BigIntegerConverter())

  register(classOf[BigDecimal], new BigDecimalConverter())

  private class ShortConverter extends Converter[String, java.lang.Short] {
    override def apply(string: String) = Numbers.toShort(string)
  }

  private class IntConverter extends Converter[String, Integer] {
    override def apply(string: String) = Numbers.toInt(string)
  }

  private class LongConverter extends Converter[String, java.lang.Long] {
    override def apply(string: String) = Numbers.toLong(string)
  }

  private class FloatConverter extends Converter[String, java.lang.Float] {
    override def apply(string: String) = Numbers.toFloat(string)
  }

  private class DoubleConverter extends Converter[String, java.lang.Double] {
    override def apply(string: String) = Numbers.toDouble(string)
  }

  private class BigIntegerConverter extends Converter[String, BigInteger] {

    override def apply(string: String): BigInteger = {
      try {
        new BigInteger(string)
      } catch {
        case e: NumberFormatException => null
      }
    }
  }

  private class BigDecimalConverter extends Converter[String, BigDecimal] {

    override def apply(string: String): BigDecimal = {
      try {
        new BigDecimal(string)
      } catch {
        case e: NumberFormatException => null
      }
    }
  }

}
