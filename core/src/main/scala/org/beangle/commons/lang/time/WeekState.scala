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
package org.beangle.commons.lang.time

import org.beangle.commons.lang.Strings

object WeekState {
  def apply(value: String): WeekState = {
    new WeekState(java.lang.Long.parseLong(value, 2))
  }
}

class WeekState(val value: Long) extends Serializable {

  def |(other: WeekState): WeekState = {
    new WeekState(this.value | other.value)
  }

  def &(other: WeekState): WeekState = {
    new WeekState(this.value & other.value)
  }

  override def toString: String = {
    java.lang.Long.toBinaryString(value)
  }

  def span: Tuple2[Int, Int] = {
    val str = toString
    val length = str.length - 1
    new Tuple2(length - str.lastIndexOf('1'), length - str.indexOf('1'))
  }

  def weeks: Int = {
    Strings.count(toString, "1")
  }

  def first: Int = {
    val weekstr = toString.toCharArray()
    var i = 0
    while (i < weekstr.length) {
      if (weekstr(weekstr.length - i - 1) == '1') return i;
    }
    return weekstr.length
  }

  def weekList: List[Int] = {
    val weekstr = toString
    var i = weekstr.length - 1
    val result = new collection.mutable.ListBuffer[Int]
    while (i >= 0) {
      if (weekstr.charAt(i) == '1') result += (weekstr.length - 1 - i)
      i -= 1
    }
    result.toList
  }

  def isOccupied(week: Int): Boolean = {
    (value & (1l << week)) > 0
  }

}