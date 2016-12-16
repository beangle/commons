/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
import org.beangle.commons.lang.annotation.beta
import org.beangle.commons.lang.annotation.value

object WeekState {

  val Zero = new WeekState(0l)

  def apply(value: String): WeekState = {
    new WeekState(value)
  }

  def of(weekIndex: Int): WeekState = {
    return new WeekState(1l << weekIndex)
  }

  def of(weekIndecies: Iterable[Int]): WeekState = {
    var v = 0l
    for (index <- weekIndecies) {
      v |= (1l << index)
    }
    new WeekState(v)
  }

  def of(weekIndecies: Int*): WeekState = {
    of(weekIndecies)
  }
}

/**
 * week index is 1 based.
 */
@beta
@value
class WeekState(val value: Long) extends Ordered[WeekState] with Serializable {

  def this(str: String) {
    this(if (Strings.isEmpty(str)) 0 else java.lang.Long.parseLong(str, 2))
  }

  override def compare(other: WeekState): Int = {
    if (this.value < other.value) -1
    else if (this.value == other.value) 0
    else 1
  }

  def |(other: WeekState): WeekState = {
    new WeekState(this.value | other.value)
  }

  def &(other: WeekState): WeekState = {
    new WeekState(this.value & other.value)
  }

  def isOverlap(other: WeekState): Boolean = {
    (this.value & other.value) > 0
  }

  override def toString: String = {
    java.lang.Long.toBinaryString(value)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case ws: WeekState => ws.value == this.value
      case _             => false
    }
  }

  override def hashCode: Int = {
    java.lang.Long.hashCode(value)
  }

  def span: Tuple2[Int, Int] = {
    val str = toString
    val length = str.length
    (length - str.lastIndexOf('1') - 1, length - str.indexOf('1') - 1)
  }

  /**
   * how many weeks
   * @see http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetTable
   * @see http://www.geeksforgeeks.org/count-set-bits-in-an-integer/
   */
  def size: Int = {
    var c = 0
    var n = this.value
    while (n > 0) {
      // clear the least significant bit set
      n &= (n - 1)
      c += 1
    }
    c
  }

  def last: Int = {
    if (value > 0) {
      val str = toString
      str.length - str.indexOf('1') - 1
    } else {
      -1
    }
  }

  def first: Int = {
    if (value > 0) {
      val str = toString
      str.length - str.lastIndexOf('1') - 1
    } else {
      -1
    }
  }

  def weeks: List[Int] = {
    val weekstr = toString
    var i = weekstr.length - 1
    val result = new collection.mutable.ListBuffer[Int]
    while (i >= 0) {
      if (weekstr.charAt(i) == '1') result += (weekstr.length - i - 1)
      i -= 1
    }
    result.toList
  }

  def isOccupied(week: Int): Boolean = {
    (value & (1l << week)) > 0
  }

}
