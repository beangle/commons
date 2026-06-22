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

import java.math.{BigDecimal as JBigDecimal}

/** Fixed-scale decimal (5 fractional digits) stored as scaled [[Int]] for compact small magnitudes.
 *
 * Encoding: `value = round(actual x 10^5)` using HALF_UP. Same semantics as [[Decimal5]], but
 * backed by 32-bit scaled integer.
 *
 * Value range (actual):
 *  - minimum: -21_474.83648 (`value = Int.MinValue`)
 *  - maximum:  21_474.83647 (`value = Int.MaxValue`)
 *
 * Recommended database column: `DECIMAL(10, 5)` / `NUMERIC(10, 5)` storing the actual decimal,
 * via JDBC `getBigDecimal` / `setBigDecimal` and `of` / [[toBigDecimal]].
 */
object TinyDecimal5 {

  val Zero: TinyDecimal5 = new TinyDecimal5(0)

  /** Minimum representable value (actual -21_474.83648). */
  val MinValue: TinyDecimal5 = new TinyDecimal5(Int.MinValue)

  /** Maximum representable value (actual 21_474.83647). */
  val MaxValue: TinyDecimal5 = new TinyDecimal5(Int.MaxValue)

  /** Wraps an already scaled int value (not the actual decimal). */
  def apply(raw: Int): TinyDecimal5 = new TinyDecimal5(raw)

  def of(n: JBigDecimal): TinyDecimal5 = apply(ScaledDecimal.ofBigDecimalInt(n))

  def of(s: String): TinyDecimal5 = apply(ScaledDecimal.ofStringInt(s))

  def of(n: Double): TinyDecimal5 = apply(ScaledDecimal.ofDoubleInt(n))

  def of(n: Float): TinyDecimal5 = apply(ScaledDecimal.ofFloatInt(n))
}

/** Compact fixed 5-decimal value type backed by [[Int]]; see [[TinyDecimal5]] companion. */
@value
class TinyDecimal5(val value: Int) extends Number, Ordered[TinyDecimal5], Serializable {

  override def longValue(): Long = ScaledDecimal.integerPartInt(value).toLong

  override def intValue(): Int = ScaledDecimal.integerPartInt(value)

  override def shortValue(): Short = intValue().toShort

  override def byteValue(): Byte = intValue().toByte

  override def doubleValue(): Double = value.toDouble / ScaledDecimal.FactorInt

  override def floatValue(): Float = doubleValue().toFloat

  def +(other: TinyDecimal5): TinyDecimal5 = TinyDecimal5(Math.addExact(value, other.value))

  def -(other: TinyDecimal5): TinyDecimal5 = TinyDecimal5(Math.subtractExact(value, other.value))

  def unary_- : TinyDecimal5 = {
    if value == Int.MinValue then
      throw new ArithmeticException("overflow")
    TinyDecimal5(-value)
  }

  override def compare(that: TinyDecimal5): Int = Integer.compare(value, that.value)

  def toBigDecimal: JBigDecimal = ScaledDecimal.toBigDecimal(value)

  def toDecimal5: Decimal5 = Decimal5(value.toLong)

  override def toString: String = ScaledDecimal.format(value)

  override def equals(obj: Any): Boolean = obj match {
    case d: TinyDecimal5 => d.value == value
    case _ => false
  }

  override def hashCode: Int = value
}
