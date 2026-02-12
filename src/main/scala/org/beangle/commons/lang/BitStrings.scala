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

/** BitStrings class.
 *
 * @author chaostone
 */
object BitStrings {

  /** Bitwise AND of two equal-length strings. Result bit is 1 only when both input bits are non-zero.
   *
   * @param first  the first bit string
   * @param second the second bit string
   * @return the result bit string
   */
  def and(first: String, second: String): String = {
    val buffer = new StringBuilder()
    for (i <- 0 until first.length)
      if ('0' == first.charAt(i) || '0' == second.charAt(i)) buffer.append('0')
      else buffer.append('1')
    buffer.toString
  }

  /** Bitwise OR of two equal-length strings containing only '1' and '0'.
   *
   * @param first  the first bit string
   * @param second the second bit string
   * @return the result bit string
   */
  def or(first: String, second: String): String = {
    val buffer = new StringBuilder()
    for (i <- 0 until first.length)
      if ('0' == first.charAt(i) && '0' == second.charAt(i)) buffer.append('0')
      else buffer.append('1')
    buffer.toString
  }

  /** Masks str by boolString: keep str(i) if boolString(i)!='0', otherwise set to '0'.
   *
   * @param str        the string to mask
   * @param boolString the mask (0/1 string)
   * @return the masked string
   */
  def andWith(str: String, boolString: String): String = {
    if (Strings.isEmpty(str)) return null
    if (Strings.isEmpty(boolString)) return str
    if (str.length < boolString.length) return str
    val buffer = new StringBuilder(str)
    for (i <- 0 until buffer.length if boolString.charAt(i) == '0') buffer.setCharAt(i, '0')
    buffer.toString
  }

  /** Converts each non-zero character to '1', zero to '0' (e.g. "3142" -> "1111").
   *
   * @param first the input string
   * @return the binary-like string
   */
  def convertToBoolStr(first: String): String = {
    val occupyBuffer = new StringBuilder(first.length)
    for (i <- 0 until first.length)
      if ('0' == first.charAt(i)) occupyBuffer.append('0')
      else occupyBuffer.append('1')
    occupyBuffer.toString
  }

  /** Parses a binary string ('0'/'1') to long value.
   *
   * @param binaryStr the binary string
   * @return the numeric value
   */
  def binValueOf(binaryStr: String): Long = {
    if (Strings.isEmpty(binaryStr)) return 0
    var value = 0
    var height = 1
    var i = binaryStr.length - 1
    while (i >= 0) {
      if ('1' == binaryStr.charAt(i)) value += height
      height *= 2
      i -= 1
    }
    value
  }

  /** Reverse.
   *
   * @param binaryStr a String object.
   * @return a String object.
   */
  def reverse(binaryStr: String): String = {
    val occupyBuffer = new StringBuilder(binaryStr.length)
    for (i <- 0 until binaryStr.length)
      if ('0' == binaryStr.charAt(i)) occupyBuffer.append('1')
      else occupyBuffer.append('0')
    occupyBuffer.toString
  }
}
