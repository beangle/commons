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

import org.beangle.commons.text.seq.RomanSeqStyle.*

import scala.collection.mutable

object RomanSeqStyle {
  // 支持的最大数字
  val MAX: Int = 99999
}

/** 罗马数字风格的序列号
  * I(1)，V(5)，X(10)，L(50)，C(100)，D(500)，M(1000)
  */
class RomanSeqStyle extends SeqNumStyle {

  private val levels = Array(Array("I", "V", "X"), Array("X", "L", "C"), Array("C", "D", "M"))

  override def build(seq: Int): String = {
    if (seq > MAX)
      throw new RuntimeException("seq greate than " + MAX)
    toRoman(String.valueOf(seq))
  }

  def toRoman(n: String): String = {
    val r = new mutable.StringBuilder
    for (c <- 0 until n.length) {
      r ++= calcDigit(n.charAt(c).toString.toInt, n.length - c - 1)
    }
    r.mkString
  }

  def calcDigit(d: Integer, l: Int): String = {
    if (l > 2) {
      "M" * (d * Math.pow(10, l - 3)).toInt
    } else {
      d match {
        case 1 => levels(l)(0)
        case 2 => levels(l)(0) + levels(l)(0)
        case 3 => levels(l)(0) + levels(l)(0) + levels(l)(0)
        case 4 => levels(l)(0) + levels(l)(1)
        case 5 => levels(l)(1)
        case 6 => levels(l)(1) + levels(l)(0)
        case 7 => levels(l)(1) + levels(l)(0) + levels(l)(0)
        case 8 => levels(l)(1) + levels(l)(0) + levels(l)(0) + levels(l)(0)
        case 9 => levels(l)(0) + levels(l)(2)
        case _ => ""
      }
    }
  }

  def isNumeric(str: String): Boolean = {
    str.forall(x => '0' <= x && x <= '9')
  }
}
