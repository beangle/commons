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

package org.beangle.commons.text.seq

import org.beangle.commons.text.seq.HanZiSeqStyle.*

/** HanZiSeqStyle constants (零, 一, 二... 十, 百, 千, 万). */
object HanZiSeqStyle {

  /** Maximum supported sequence number. */
  val MAX = 99999
  /** Chinese digit characters (0-10). */
  val CHINESE_NAMES = Array("零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")

  /** Unit characters (十, 百, 千, 万). */
  val PRIORITIES = Array("十", "百", "千", "万")
}

/** Chinese character sequence generator (零, 一, 二, 三... for numbers).
 *
 * @author chaostone,zhufengbin
 */
class HanZiSeqStyle extends SeqNumStyle {

  /** Builds the Chinese character representation of the sequence number. */
  def build(seq: Int): String = {
    if (seq > MAX)
      throw new RuntimeException("seq greate than " + MAX)
    buildText(String.valueOf(seq))
  }

  /** Converts a numeric string to Chinese character representation.
   *
   * @param str1 the digit string (e.g. "123")
   * @return the Chinese number string (e.g. "一百二十三")
   */
  def buildText(str1: String): String = {
    val sb = new StringBuilder()
    for (i <- 0 until str1.length) {
      val numChar = str1.charAt(i)
      var temp = basicOf(numChar - '0')
      if (numChar - '0' > 0)
        temp = temp + priorityOf(str1.length - i)
      sb.append(temp)
    }
    var result = sb.toString
    result = result.replaceAll("零一十", "零十")
    result = result.replaceAll("零零", "零")
    result
  }

  /** Returns the Chinese character for digit 0-10.
   *
   * @param num the digit (0-10)
   * @return the Chinese character
   */
  def basicOf(num: Int): String = CHINESE_NAMES(num)

  /** Returns the Chinese unit character for the digit position (十, 百, 千, 万).
   *
   * @param index the position index
   * @return the unit character or empty string for ones/tens
   */
  def priorityOf(index: Int): String = {
    if index < 2 then ""
    else PRIORITIES(index - 2)
  }
}
