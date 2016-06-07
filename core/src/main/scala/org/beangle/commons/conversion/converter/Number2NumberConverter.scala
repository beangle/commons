/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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

import java.math.BigDecimal
import java.math.BigInteger
import org.beangle.commons.conversion.Converter
import org.beangle.commons.conversion.impl.ConverterFactory

object Number2NumberConverter {

  private class ShortConverter extends Converter[Number, java.lang.Short] {

    override def apply(number: Number): java.lang.Short = number.shortValue()
  }

  private class IntegerConverter extends Converter[Number, java.lang.Integer] {

    override def apply(number: Number): java.lang.Integer = number.intValue()
  }

  private class LongConverter extends Converter[Number, java.lang.Long] {

    override def apply(number: Number): java.lang.Long = number.longValue()
  }

  private class FloatConverter extends Converter[Number, java.lang.Float] {

    override def apply(number: Number): java.lang.Float = number.floatValue()
  }

  private class DoubleConverter extends Converter[Number, java.lang.Double] {

    override def apply(number: Number): java.lang.Double = number.doubleValue()
  }

  private class BigIntegerConverter extends Converter[Number, BigInteger] {

    override def apply(number: Number): BigInteger = BigInteger.valueOf(number.longValue())
  }

  private class BigDecimalConverter extends Converter[Number, BigDecimal] {

    override def apply(number: Number): BigDecimal = new BigDecimal(number.toString)
  }
}

import Number2NumberConverter._
/**
 * Number Converter Factory
 *
 * @author chaostone
 * @since 3.2.0
 */
class Number2NumberConverter extends ConverterFactory[Number, Number] {

  register(classOf[java.lang.Short], new ShortConverter())

  register(classOf[java.lang.Integer], new IntegerConverter())

  register(classOf[java.lang.Long], new LongConverter())

  register(classOf[java.lang.Float], new FloatConverter())

  register(classOf[java.lang.Double], new DoubleConverter())

  register(classOf[BigInteger], new BigIntegerConverter())

  register(classOf[BigDecimal], new BigDecimalConverter())
}
