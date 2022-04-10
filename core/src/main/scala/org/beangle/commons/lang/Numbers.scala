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

import java.{lang as jl, math as jm}

object Numbers {
  /**
    * <p>
    * Convert a <code>String</code> to an <code>int</code>, returning a default value if the
    * conversion fails.
    * </p>
    * <p>
    * If the string is <code>null</code>, the default value is returned.
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
    try Integer.parseInt(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /**
    * transform to int.
    *
    * @param ids an array of String objects.
    * @return an array of  int objects.
    */
  def toInt(ids: Array[String]): Array[Int] = {
    ids.map { x => toInt(x) }
  }

  def toShort(str: String, defaultValue: Short = 0): Short = {
    if (isEmpty(str)) return defaultValue
    try jl.Short.parseShort(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def toLong(str: String, defaultValue: Long = 0L): Long = {
    if (isEmpty(str)) return defaultValue
    try jl.Long.parseLong(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /**
    * transformToLong.
    *
    * @param ids an array of String objects.
    * @return an array of Long objects.
    */
  def toLong(ids: Array[String]): Array[Long] = {
    if (null == ids) return null
    val idsOfLong = new Array[Long](ids.length)
    ids.indices foreach (i => idsOfLong(i) = java.lang.Long.parseLong(ids(i)))
    idsOfLong
  }

  def toFloat(str: String, defaultValue: Float = 0.0f): Float = {
    if (isEmpty(str)) return defaultValue
    try jl.Float.parseFloat(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def toDouble(str: String, defaultValue: Double = 0.0d): Double = {
    if (isEmpty(str)) return defaultValue
    try jl.Double.parseDouble(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  // convert string to number object
  def convert2Int(str: String, defaultValue: jl.Integer = null): jl.Integer = {
    if isEmpty(str) then return defaultValue
    try Integer.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2Short(str: String, defaultValue: jl.Short = null): jl.Short = {
    if (isEmpty(str)) return defaultValue
    try jl.Short.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2Long(str: String, defaultValue: jl.Long = null): jl.Long = {
    if (isEmpty(str)) return defaultValue
    try jl.Long.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2Float(str: String, defaultValue: jl.Float = null): jl.Float = {
    if (isEmpty(str)) return defaultValue
    try jl.Float.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2Double(str: String, defaultValue: jl.Double = null): jl.Double = {
    if (isEmpty(str)) return defaultValue
    try jl.Double.valueOf(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2BigInt(str: String, defaultValue: jm.BigInteger = null): jm.BigInteger = {
    if (isEmpty(str)) return defaultValue
    try new jm.BigInteger(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  def convert2BigDecimal(str: String, defaultValue: jm.BigDecimal = null): jm.BigDecimal = {
    if (isEmpty(str)) return defaultValue
    try new jm.BigDecimal(str)
    catch case nfe: NumberFormatException => defaultValue
  }

  /**
    * <p>
    * Checks whether the <code>String</code> contains only digit characters.
    * </p>
    * <p>
    * <code>Null</code> and empty String will return <code>false</code>.
    * </p>
    *
    * @param str the <code>String</code> to check
    * @return <code>true</code> if str contains only Unicode numeric
    */
  def isDigits(str: String): Boolean = {
    if isEmpty(str) then return false
    (0 until str.length).forall(i => Character.isDigit(str.charAt(i)))
  }
}
