/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang

object Numbers {

  /**
   * <p>
   * Convert a <code>String</code> to an <code>int</code>, returning <code>zero</code> if the
   * conversion fails.
   * </p>
   * <p>
   * If the string is <code>null</code>, <code>zero</code> is returned.
   * </p>
   *
   * <pre>
   * toInt(null) = 0
   * toInt("")   = 0
   * toInt("1")  = 1
   * </pre>
   *
   * @param str the string to convert, may be null
   * @return the int represented by the string, or <code>zero</code> if
   *         conversion fails
   * @since 3.0
   */
  def toInt(str: String): Int = {
    if (str == null) return 0
    try {
      Integer.valueOf(str)
    } catch {
      case nfe: NumberFormatException => 0
    }
  }

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
   * @param str the string to convert, may be null
   * @param defaultValue the default value
   * @return the int represented by the string, or the default if conversion fails
   * @since 3.0
   */
  def toInt(str: String, defaultValue: Int): Int = {
    if (str == null) return defaultValue
    try {
      Integer.valueOf(str)
    } catch {
      case nfe: NumberFormatException => defaultValue
    }
  }

  def toShort(str: String): Short = {
    if (str == null) return 0
    try {
      java.lang.Short.valueOf(str)
    } catch {
      case nfe: NumberFormatException => 0
    }
  }

  def toLong(str: String): Long = {
    if (str == null) return 0l
    try {
      java.lang.Long.valueOf(str)
    } catch {
      case nfe: NumberFormatException => 0l
    }
  }

  def toFloat(str: String): Float = {
    if (str == null) return 0.0f;
    try {
      java.lang.Float.valueOf(str)
    } catch {
      case nfe: NumberFormatException => 0.0f
    }
  }

  def toDouble(str: String): Double = {
    if (str == null) return 0.0d
    try {
      java.lang.Double.valueOf(str)
    } catch {
      case nfe: NumberFormatException => 0.0d
    }
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
    if (Strings.isEmpty(str)) return false

    for (i <- 0 until str.length if !Character.isDigit(str.charAt(i))) {
      return false
    }
    true
  }
}
