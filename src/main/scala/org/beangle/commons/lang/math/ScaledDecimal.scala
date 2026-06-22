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

package org.beangle.commons.lang.math

import org.beangle.commons.lang.Strings

import java.math.RoundingMode
import java.{lang as jl, math as jm}

/** Internal helpers for fixed-scale decimal value types (scale = 5). */
private object ScaledDecimal {

  val Scale = 5

  val FactorLong: Long = 100_000L

  val FactorInt: Int = 100_000

  def scaleToLong(n: jm.BigDecimal): Long = {
    n.setScale(Scale, RoundingMode.HALF_UP).movePointRight(Scale).longValueExact()
  }

  def scaleToInt(n: jm.BigDecimal): Int = {
    n.setScale(Scale, RoundingMode.HALF_UP).movePointRight(Scale).intValueExact()
  }

  def ofBigDecimalLong(n: jm.BigDecimal): Long = scaleToLong(n)

  def ofBigDecimalInt(n: jm.BigDecimal): Int = scaleToInt(n)

  def ofStringLong(s: String): Long =
    if Strings.isBlank(s) then 0L else scaleToLong(new jm.BigDecimal(s.trim))

  def ofStringInt(s: String): Int =
    if Strings.isBlank(s) then 0 else scaleToInt(new jm.BigDecimal(s.trim))

  def ofDoubleLong(n: Double): Long = scaleToLong(jm.BigDecimal(jl.Double.toString(n)))

  def ofDoubleInt(n: Double): Int = scaleToInt(jm.BigDecimal(jl.Double.toString(n)))

  def ofFloatLong(n: Float): Long = scaleToLong(jm.BigDecimal(jl.Float.toString(n)))

  def ofFloatInt(n: Float): Int = scaleToInt(jm.BigDecimal(jl.Float.toString(n)))

  def toBigDecimal(value: Long): jm.BigDecimal = jm.BigDecimal.valueOf(value, Scale)

  def toBigDecimal(value: Int): jm.BigDecimal = jm.BigDecimal.valueOf(value.toLong, Scale)

  def format(value: Long): String = stripTrailingZeros(toBigDecimal(value))

  def format(value: Int): String = stripTrailingZeros(toBigDecimal(value))

  private def stripTrailingZeros(n: jm.BigDecimal): String = n.stripTrailingZeros().toPlainString

  def integerPartLong(value: Long): Long = value / FactorLong

  def integerPartInt(value: Int): Int = value / FactorInt
}

