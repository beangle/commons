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

package org.beangle.commons.conversion.converter

import org.beangle.commons.conversion.Converter
import org.beangle.commons.conversion.impl.ConverterFactory

import java.math.{BigDecimal, BigInteger}

object Number2NumberConverter extends ConverterFactory[Number, Number] {

  private object ShortConverter extends Converter[Number, java.lang.Short] {
    override def apply(number: Number): java.lang.Short = number.shortValue()
  }

  private object IntegerConverter extends Converter[Number, java.lang.Integer] {
    override def apply(number: Number): java.lang.Integer = number.intValue()
  }

  private object LongConverter extends Converter[Number, java.lang.Long] {
    override def apply(number: Number): java.lang.Long = number.longValue()
  }

  private object FloatConverter extends Converter[Number, java.lang.Float] {
    override def apply(number: Number): java.lang.Float = number.floatValue()
  }

  private object DoubleConverter extends Converter[Number, java.lang.Double] {
    override def apply(number: Number): java.lang.Double = number.doubleValue()
  }

  private object BigIntegerConverter extends Converter[Number, BigInteger] {
    override def apply(number: Number): BigInteger = BigInteger.valueOf(number.longValue)
  }

  private object BigDecimalConverter extends Converter[Number, BigDecimal] {
    override def apply(number: Number): BigDecimal = BigDecimal(number.toString)
  }

  register(classOf[java.lang.Short], ShortConverter)

  register(classOf[java.lang.Integer], IntegerConverter)

  register(classOf[java.lang.Long], LongConverter)

  register(classOf[java.lang.Float], FloatConverter)

  register(classOf[java.lang.Double], DoubleConverter)

  register(classOf[BigInteger], BigIntegerConverter)

  register(classOf[BigDecimal], BigDecimalConverter)
}
