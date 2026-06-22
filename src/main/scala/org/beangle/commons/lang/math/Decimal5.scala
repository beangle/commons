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

import org.beangle.commons.lang.annotation.value

import java.math.BigDecimal as JBigDecimal

/** Fixed-scale decimal (5 fractional digits) stored as scaled [[Long]].
 *
 * Encoding: `value = round(actual x 10^5)` using HALF_UP. The full 64 bits are a signed scaled
 * integer; fractional precision is a type-level convention, not a bit layout.
 *
 * Value range (actual):
 *  - minimum: -92_233_720_368_547.75808 (`value = Long.MinValue`)
 *  - maximum:  92_233_720_368_547.75807 (`value = Long.MaxValue`)
 *
 * Recommended database column: `DECIMAL(19, 5)` / `NUMERIC(19, 5)` storing the actual decimal
 * (e.g. `12.34567`), via JDBC `getBigDecimal` / `setBigDecimal` and `of` / [[toBigDecimal]].
 */
object Decimal5 {

  val Zero: Decimal5 = new Decimal5(0L)

  /** Minimum representable value (actual -92_233_720_368_547.75808). */
  val MinValue: Decimal5 = new Decimal5(Long.MinValue)

  /** Maximum representable value (actual 92_233_720_368_547.75807). */
  val MaxValue: Decimal5 = new Decimal5(Long.MaxValue)

  /** Wraps an already scaled long value (not the actual decimal). */
  def apply(raw: Long): Decimal5 = new Decimal5(raw)

  def of(n: JBigDecimal): Decimal5 = apply(ScaledDecimal.ofBigDecimalLong(n))

  def of(s: String): Decimal5 = apply(ScaledDecimal.ofStringLong(s))

  def of(n: Double): Decimal5 = apply(ScaledDecimal.ofDoubleLong(n))

  def of(n: Float): Decimal5 = apply(ScaledDecimal.ofFloatLong(n))

  def of(tiny: TinyDecimal5): Decimal5 = apply(tiny.value.toLong)
}

/** Fixed 5-decimal value type backed by [[Long]]; see [[Decimal5]] companion for range and DB mapping. */
@value
class Decimal5(val value: Long) extends Number, Ordered[Decimal5], Serializable {

  override def longValue(): Long = ScaledDecimal.integerPartLong(value)

  override def intValue(): Int = longValue().toInt

  override def shortValue(): Short = longValue().toShort

  override def byteValue(): Byte = longValue().toByte

  override def doubleValue(): Double = value.toDouble / ScaledDecimal.FactorLong

  override def floatValue(): Float = doubleValue().toFloat

  def +(other: Decimal5): Decimal5 = Decimal5(Math.addExact(value, other.value))

  def -(other: Decimal5): Decimal5 = Decimal5(Math.subtractExact(value, other.value))

  def unary_- : Decimal5 = {
    if value == Long.MinValue then
      throw new ArithmeticException("overflow")
    Decimal5(-value)
  }

  override def compare(that: Decimal5): Int = java.lang.Long.compare(value, that.value)

  def toBigDecimal: JBigDecimal = ScaledDecimal.toBigDecimal(value)

  def toTinyDecimal5: TinyDecimal5 = {
    if value > Int.MaxValue || value < Int.MinValue then
      throw new ArithmeticException(s"value $value out of Int range for TinyDecimal5")
    TinyDecimal5(value.toInt)
  }

  override def toString: String = ScaledDecimal.format(value)

  override def equals(obj: Any): Boolean = obj match {
    case d: Decimal5 => d.value == value
    case _ => false
  }

  override def hashCode: Int = java.lang.Long.hashCode(value)
}

