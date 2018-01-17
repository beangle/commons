/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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

/**
 * BitStrings class.
 *
 * @author chaostone
 */
object BitStrings {

  /**
   * 比较两个等长字符串的每一位，若都大于0，则返回结果的相应位为1，否则为0;
   *
   * @param first a String.
   * @param second a String.
   * @return a String.
   */
  def and(first: String, second: String): String = {
    val buffer = new StringBuilder()
    for (i <- 0 until first.length) {
      if ('0' == first.charAt(i) || '0' == second.charAt(i)) buffer.append('0')
      else buffer.append('1')
    }
    buffer.toString
  }

  /**
   * 比较两个等长字符串的每一位，相或<br>
   * 适用于仅含有1和0的字符串.
   *
   * @param first a String.
   * @param second a String.
   * @return a String.
   */
  def or(first: String, second: String): String = {
    val buffer = new StringBuilder()
    for (i <- 0 until first.length) {
      if ('0' == first.charAt(i) && '0' == second.charAt(i)) buffer.append('0')
      else buffer.append('1')
    }
    buffer.toString
  }

  /**
   * 将一个字符串，按照boolString的形式进行变化. 如果boolString[i]!=0则保留str[i],否则置0
   *
   * @param str a String.
   * @param boolString a String.
   * @return a String.
   */
  def andWith(str: String, boolString: String): String = {
    if (Strings.isEmpty(str)) return null
    if (Strings.isEmpty(boolString)) return str
    if (str.length < boolString.length) return str
    val buffer = new StringBuilder(str)
    for (i <- 0 until buffer.length if boolString.charAt(i) == '0') buffer.setCharAt(i, '0')
    buffer.toString
  }

  /**
   * 将"314213421340asdf"转换成"1111111111101111"
   *
   * @param first a String object.
   * @return a String object.
   */
  def convertToBoolStr(first: String): String = {
    val occupyBuffer = new StringBuilder(first.length)
    for (i <- 0 until first.length) {
      if ('0' == first.charAt(i)) occupyBuffer.append('0')
      else occupyBuffer.append('1')
    }
    occupyBuffer.toString
  }

  /**
   * 返回零一串的整型值
   *
   * @param binaryStr a String object.
   * @return a long.
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

  /**
   * <p>
   * reverse.
   * </p>
   *
   * @param binaryStr a String object.
   * @return a String object.
   */
  def reverse(binaryStr: String): String = {
    val occupyBuffer = new StringBuilder(binaryStr.length)
    for (i <- 0 until binaryStr.length) {
      if ('0' == binaryStr.charAt(i)) occupyBuffer.append('1')
      else occupyBuffer.append('0')
    }
    occupyBuffer.toString
  }
}