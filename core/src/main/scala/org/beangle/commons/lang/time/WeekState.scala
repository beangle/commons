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

package org.beangle.commons.lang.time

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.value

enum WeekCycle {

  case Continuous, Odd, Even, Random

  def id: Int = ordinal + 1
}

object WeekState {

  val Zero = new WeekState(0L)

  def apply(raw: String): WeekState = new WeekState(raw)

  /** Build a weekstate by cycle
   *
   * @param start start week 1 based
   * @param weeks how may weeks
   * @param cycle cycle type(1"连续周", 2"单周", 3"双周", 4"任意周")
   * @return
   */
  def build(start: Int, weeks: Int, cycle: WeekCycle): WeekState = {
    val maxWeek = start + weeks - 1
    require(maxWeek <= 63, "Weekstate only support max week is 63")
    var v = 0L
    (0 until weeks).foreach { i =>
      val add = cycle match
        case WeekCycle.Odd => (start + i) % 2 == 1
        case WeekCycle.Even => (start + i) % 2 == 0
        case _ => true
      if add then v |= 1L << (start + i)
    }
    new WeekState(v)
  }

  def of(weekIndex: Int): WeekState = new WeekState(1L << weekIndex)

  def of(weekIndecies: Iterable[Int]): WeekState = {
    var v = 0L
    for (index <- weekIndecies) v |= (1L << index)
    new WeekState(v)
  }

  def of(weekIndecies: Int*): WeekState = of(weekIndecies)
}

/** Assembly week in a long value.
 * week index is 1 based.
 */
@value
class WeekState(val value: Long) extends Ordered[WeekState] with Serializable {

  def this(raw: String) = {
    this(if (Strings.isEmpty(raw)) 0 else java.lang.Long.parseLong(raw, 2))
  }

  override def compare(other: WeekState): Int = {
    if (this.value < other.value) -1
    else if (this.value == other.value) 0
    else 1
  }

  def |(other: WeekState): WeekState = new WeekState(this.value | other.value)

  def &(other: WeekState): WeekState = new WeekState(this.value & other.value)

  def ^(other: WeekState): WeekState = new WeekState(this.value ^ other.value)

  def isOverlap(other: WeekState): Boolean = (this.value & other.value) > 0

  override def toString: String = java.lang.Long.toBinaryString(value)

  override def equals(obj: Any): Boolean = {
    obj match {
      case ws: WeekState => ws.value == this.value
      case _ => false
    }
  }

  override def hashCode: Int = java.lang.Long.hashCode(value)

  def span: (Int, Int) = {
    val str = toString
    val length = str.length
    (length - str.lastIndexOf('1') - 1, length - str.indexOf('1') - 1)
  }

  /** how many weeks
   *
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
    if value > 0 then
      val str = toString
      str.length - str.indexOf('1') - 1
    else -1
  }

  def first: Int = {
    if value > 0 then
      val str = toString
      str.length - str.lastIndexOf('1') - 1
    else -1
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

  def contains(week: Int): Boolean = (value & (1L << week)) > 0

  def cycle: WeekCycle = {
    val wl = this.weeks
    if (wl.isEmpty) return WeekCycle.Continuous

    val first = wl.head
    val last = wl.last
    if (last - first + 1 == wl.size) return WeekCycle.Continuous

    var oddWeeks = 0
    var evenWeeks = 0
    wl foreach { i =>
      if (i % 2 == 1) oddWeeks += 1
      else if (i % 2 == 0) evenWeeks += 1
    }
    if (evenWeeks == 0) WeekCycle.Odd
    else if (oddWeeks == 0) WeekCycle.Even
    else WeekCycle.Random
  }
}
