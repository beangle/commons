/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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

import org.beangle.commons.lang.Numbers
import org.beangle.commons.conversion.Converter
import java.{ lang => jl }
import java.{ math => jm }
/**
 * Convert string to number.
 *
 * @author chaostone
 * @since 3.2.0
 */
class String2NumberConverter extends StringConverterFactory[String, Number] {

  register(classOf[jl.Short], new ShortConverter())

  register(classOf[jl.Integer], new IntConverter())

  register(classOf[jl.Long], new LongConverter())

  register(classOf[jl.Float], new FloatConverter())

  register(classOf[jl.Double], new DoubleConverter())

  register(classOf[jm.BigInteger], new BigIntegerConverter())

  register(classOf[jm.BigDecimal], new BigDecimalConverter())

  private class ShortConverter extends Converter[String, jl.Short] {
    override def apply(str: String): jl.Short = {
      Numbers.convert2Short(str, null)
    }
  }

  private class IntConverter extends Converter[String, Integer] {
    override def apply(str: String): jl.Integer = {
      Numbers.convert2Int(str, null)
    }
  }

  private class LongConverter extends Converter[String, jl.Long] {
    override def apply(str: String): jl.Long = {
      Numbers.convert2Long(str, null)
    }
  }

  private class FloatConverter extends Converter[String, jl.Float] {
    override def apply(str: String): jl.Float = {
      Numbers.convert2Float(str, null)
    }
  }

  private class DoubleConverter extends Converter[String, jl.Double] {
    override def apply(str: String): jl.Double = {
      Numbers.convert2Double(str, null)
    }
  }

  private class BigIntegerConverter extends Converter[String, jm.BigInteger] {

    override def apply(str: String): jm.BigInteger = {
      Numbers.convert2BigInt(str, null)
    }
  }

  private class BigDecimalConverter extends Converter[String, jm.BigDecimal] {

    override def apply(str: String): jm.BigDecimal = {
      Numbers.convert2BigDecimal(str, null)
    }
  }

}
