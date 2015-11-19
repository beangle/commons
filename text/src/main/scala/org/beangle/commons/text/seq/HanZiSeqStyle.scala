/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.text.seq

import HanZiSeqStyle._

object HanZiSeqStyle {

  /**
   * Constant <code>MAX=99999</code>
   */
  val MAX = 99999

  /**
   * Constant <code>CHINESE_NAMES="{ 零, 一, 二, 三, 四, 五, 六, 七, 八, 九, 十 }"</code>
   */
  val CHINESE_NAMES = Array("零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")

  /**
   * Constant <code>PRIORITIES="{ 十, 百, 千, 万 }"</code>
   */
  val PRIORITIES = Array("十", "百", "千", "万")
}

/**
 * 汉字序列产生器
 *
 * @author chaostone,zhufengbin
 */
class HanZiSeqStyle extends SeqNumStyle {

  /**
   * {@inheritDoc}
   */
  def build(seq: Int): String = {
    if (seq > MAX) {
      throw new RuntimeException("seq greate than " + MAX)
    }
    buildText(String.valueOf(seq))
  }

  /**
   * <p>
   * basicOf.
   * </p>
   *
   * @param num a int.
   * @return a String object.
   */
  def basicOf(num: Int): String = CHINESE_NAMES(num)

  /**
   * <p>
   * priorityOf.
   * </p>
   *
   * @param index a int.
   * @return a String object.
   */
  def priorityOf(index: Int): String = {
    if (index < 2) {
      ""
    } else {
      PRIORITIES(index - 2)
    }
  }

  /**
   * <p>
   * buildText.
   * </p>
   *
   * @param str1 a String object.
   * @return a String object.
   */
  def buildText(str1: String): String = {
    val sb = new StringBuilder()
    for (i <- 0 until str1.length) {
      val numChar = str1.charAt(i)
      var temp = basicOf(numChar - '0')
      if (numChar - '0' > 0) {
        temp = temp + priorityOf(str1.length - i)
      }
      sb.append(temp)
    }
    var result = sb.toString
    result = result.replaceAll("零一十", "零十")
    result = result.replaceAll("零零", "零")
    result
  }
}
