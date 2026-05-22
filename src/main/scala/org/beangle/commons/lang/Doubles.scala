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

package org.beangle.commons.lang

import org.beangle.commons.lang.annotation.beta

import java.math.RoundingMode
import java.{lang as jl, math as jm}

@beta
object Doubles {

  /** Rounds a double value to the specified decimal scale using HALF_UP rounding.
   *
   * @param n     the value to round
   * @param scale number of decimal places
   * @return rounded value
   */
  def round(n: Double, scale: Int): Double = {
    val b = jm.BigDecimal(jl.Double.toString(n))
    b.setScale(scale, RoundingMode.HALF_UP).doubleValue
  }

  /** Adds two double values with BigDecimal precision to avoid floating-point errors.
   *
   * @param v1 first value
   * @param v2 second value
   * @return sum
   */
  def add(v1: Double, v2: Double): Double = {
    val b1 = jm.BigDecimal(jl.Double.toString(v1))
    val b2 = jm.BigDecimal(jl.Double.toString(v2))
    b1.add(b2).doubleValue
  }

  /** Subtracts v2 from v1 with BigDecimal precision to avoid floating-point errors.
   *
   * @param v1 minuend
   * @param v2 subtrahend
   * @return difference
   */
  def subtract(v1: Double, v2: Double): Double = {
    val b1 = jm.BigDecimal(jl.Double.toString(v1))
    val b2 = jm.BigDecimal(jl.Double.toString(v2))
    b1.subtract(b2).doubleValue
  }

  /** Multiplies two double values with BigDecimal precision to avoid floating-point errors.
   *
   * @param v1 first factor
   * @param v2 second factor
   * @return product
   */
  def multiply(v1: Double, v2: Double): Double = {
    val b1 = jm.BigDecimal(jl.Double.toString(v1))
    val b2 = jm.BigDecimal(jl.Double.toString(v2))
    b1.multiply(b2).doubleValue
  }

  /** Divides v1 by v2 with BigDecimal precision and rounding.
   *
   * @param v1    dividend
   * @param v2    divisor
   * @param scale decimal places for result (default 10)
   * @return quotient
   */
  def divide(v1: Double, v2: Double, scale: Int = 10): Double = {
    val b1 = jm.BigDecimal(jl.Double.toString(v1))
    val b2 = jm.BigDecimal(jl.Double.toString(v2))
    b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue
  }

  def isZero(x: Double): Boolean = {
    equals(x, 0, 1e-6)
  }

  def compare(x: Double, y: Double, eps: Double): Int = {
    if equals(x, y, eps) then 0
    else if x < y then -1
    else 1
  }

  def equals(x: Double, y: Double, eps: Double): Boolean = {
    ulpEquals(x, y, 1) || Math.abs(y - x) <= eps
  }

  def equals(v1: Double, v2: Double): Boolean = {
    ulpEquals(v1, v2, 1)
  }

  def ulpEquals(x: Double, y: Double, maxUlps: Int): Boolean = {
    // NaN 检查
    if (jl.Double.isNaN(x) || jl.Double.isNaN(y)) return false
    // 无穷大检查
    if (jl.Double.isInfinite(x) || jl.Double.isInfinite(y)) return x == y
    // 获取原始位表示
    var xBits = jl.Double.doubleToRawLongBits(x)
    var yBits = jl.Double.doubleToRawLongBits(y)
    // 处理 -0.0 和 +0.0（它们位表示不同但数值相等）
    if (xBits == 0x8000000000000000L) xBits = 0L // -0.0 -> +0.0
    if (yBits == 0x8000000000000000L) yBits = 0L
    // 计算 ULP 距离
    val distance = Math.abs(xBits - yBits)
    distance <= maxUlps
  }
}
