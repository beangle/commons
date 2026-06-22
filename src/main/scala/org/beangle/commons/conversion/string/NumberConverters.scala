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
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.math.{Decimal5, TinyDecimal5}

import java.{lang as jl, math as jm}

/** Converts string to Short/Integer/Long/Float/Double/BigInteger/BigDecimal/Decimal5/TinyDecimal5.
 *
 * @author chaostone
 * @since 3.2.0
 */
object NumberConverters extends StringConverterFactory[String, Number] {

  private object ShortConverter extends Converter[String, jl.Short] {
    override def apply(str: String): jl.Short = Numbers.convert2Short(str, null)
  }

  private object IntConverter extends Converter[String, Integer] {
    override def apply(str: String): jl.Integer = Numbers.convert2Int(str, null)
  }

  private object LongConverter extends Converter[String, jl.Long] {
    override def apply(str: String): jl.Long = Numbers.convert2Long(str, null)
  }

  private object FloatConverter extends Converter[String, jl.Float] {
    override def apply(str: String): jl.Float = Numbers.convert2Float(str, null)
  }

  private object DoubleConverter extends Converter[String, jl.Double] {
    override def apply(str: String): jl.Double = Numbers.convert2Double(str, null)
  }

  private object BigIntegerConverter extends Converter[String, jm.BigInteger] {
    override def apply(str: String): jm.BigInteger = Numbers.convert2BigInt(str, null)
  }

  private object BigDecimalConverter extends Converter[String, jm.BigDecimal] {
    override def apply(str: String): jm.BigDecimal = Numbers.convert2BigDecimal(str, null)
  }

  private object Decimal5Converter extends Converter[String, Decimal5] {
    override def apply(str: String): Decimal5 =
      try Decimal5.of(str)
      catch case _: NumberFormatException | _: ArithmeticException => null
  }

  private object TinyDecimal5Converter extends Converter[String, TinyDecimal5] {
    override def apply(str: String): TinyDecimal5 =
      try TinyDecimal5.of(str)
      catch case _: NumberFormatException | _: ArithmeticException => null
  }

  register(classOf[jl.Short], ShortConverter)

  register(classOf[jl.Integer], IntConverter)

  register(classOf[jl.Long], LongConverter)

  register(classOf[jl.Float], FloatConverter)

  register(classOf[jl.Double], DoubleConverter)

  register(classOf[jm.BigInteger], BigIntegerConverter)

  register(classOf[jm.BigDecimal], BigDecimalConverter)

  register(classOf[Decimal5], Decimal5Converter)

  register(classOf[TinyDecimal5], TinyDecimal5Converter)
}
