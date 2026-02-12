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

import org.beangle.commons.lang.Strings.isEmpty

import java.math.RoundingMode
import java.{lang as jl, math as jm}

object Numbers {
  /** Convert a `String` to an `int`, returning a default value if the
   * conversion fails.
   * <p>
   * If the string is `null`, the default value is returned.
   * </p>
   *
   * <pre>
   * toInt(null, 1) = 1
   * toInt("", 1)   = 1
   * toInt("1", 0)  = 1
   * </pre>
   *
   * @param str          the string to convert, may be null
   * @param defaultValue the default value
   * @return the int represented by the string, or the default if conversion fails
   * @since 3.0
   */
  def toInt(str: String, defaultValue: Int = 0): Int = {
    if (isEmpty(str)) return defaultValue
    try java.lang.Double.parseDouble(str).toInt
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts each string in the array to int. Uses default 0 for invalid entries.
   *
   * @param ids the array of strings to convert
   * @return the array of int values
   */
  def toInt(ids: Array[String]): Array[Int] = {
    ids.map { x => toInt(x) }
  }

  /** Converts string to Short, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Short or default
   */
  def toShort(str: String, defaultValue: Short = 0): Short = {
    if (isEmpty(str)) return defaultValue
    try jl.Short.parseShort(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Long, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Long or default
   */
  def toLong(str: String, defaultValue: Long = 0L): Long = {
    if (isEmpty(str)) return defaultValue
    try jl.Long.parseLong(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts each string in the array to long. Throws on invalid entries.
   *
   * @param ids the array of strings to convert
   * @return the array of long values, or null if ids is null
   */
  def toLong(ids: Array[String]): Array[Long] = {
    if (null == ids) return null
    val idsOfLong = new Array[Long](ids.length)
    ids.indices foreach (i => idsOfLong(i) = java.lang.Long.parseLong(ids(i)))
    idsOfLong
  }

  /** Converts string to Float, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Float or default
   */
  def toFloat(str: String, defaultValue: Float = 0.0f): Float = {
    if (isEmpty(str)) return defaultValue
    try jl.Float.parseFloat(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Double, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Double or default
   */
  def toDouble(str: String, defaultValue: Double = 0.0d): Double = {
    if (isEmpty(str)) return defaultValue
    try jl.Double.parseDouble(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Integer (boxed), returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Integer or default
   */
  def convert2Int(str: String, defaultValue: jl.Integer = null): jl.Integer = {
    if isEmpty(str) then return defaultValue
    try Integer.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Short (boxed), returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Short or default
   */
  def convert2Short(str: String, defaultValue: jl.Short = null): jl.Short = {
    if (isEmpty(str)) return defaultValue
    try jl.Short.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Long (boxed), returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Long or default
   */
  def convert2Long(str: String, defaultValue: jl.Long = null): jl.Long = {
    if (isEmpty(str)) return defaultValue
    try jl.Long.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Float (boxed), returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Float or default
   */
  def convert2Float(str: String, defaultValue: jl.Float = null): jl.Float = {
    if (isEmpty(str)) return defaultValue
    try jl.Float.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to Double (boxed), returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the Double or default
   */
  def convert2Double(str: String, defaultValue: jl.Double = null): jl.Double = {
    if (isEmpty(str)) return defaultValue
    try jl.Double.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to BigInteger, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the BigInteger or default
   */
  def convert2BigInt(str: String, defaultValue: jm.BigInteger = null): jm.BigInteger = {
    if (isEmpty(str)) return defaultValue
    try new jm.BigInteger(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Converts string to BigDecimal, returning default on failure.
   *
   * @param str          the string to convert
   * @param defaultValue the default value
   * @return the BigDecimal or default
   */
  def convert2BigDecimal(str: String, defaultValue: jm.BigDecimal = null): jm.BigDecimal = {
    if (isEmpty(str)) return defaultValue
    try new jm.BigDecimal(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /** Checks whether the `String` contains only digit characters.
   * <p>
   * `null` and empty String will return `false`.
   * </p>
   *
   * @param str the `String` to check
   * @return `true` if str contains only Unicode numeric
   */
  def isDigits(str: String): Boolean = {
    if isEmpty(str) then return false
    val start = if str.charAt(0) == '-' then 1 else 0
    (start until str.length).forall(i => Character.isDigit(str.charAt(i)))
  }

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
}
