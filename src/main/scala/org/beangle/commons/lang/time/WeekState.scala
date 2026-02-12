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
import org.beangle.commons.lang.math.IntSeg

/** Week cycle type for WeekState.build (consecutive, odd, even, random). */
enum WeekCycle {

  case Continuous, Odd, Even, Random

  /** Numeric id for this cycle (1-based). */
  def id: Int = ordinal + 1
}

/** WeekState factory and constants. */
object WeekState {

  /** Empty WeekState (no weeks set). */
  val Zero = new WeekState(0L)

  /** Parses raw string (binary or IntSeg notation) to WeekState. */
  def apply(raw: String): WeekState = new WeekState(raw)

  /** Builds WeekState by cycle.
   *
   * @param start start week (1-based)
   * @param weeks number of weeks
   * @param cycle cycle type (Continuous, Odd, Even, Random)
   * @return WeekState with bits set for selected weeks
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

  /** Builds WeekState from a single week index.
   *
   * @param weekIndex 0-based week index
   * @return WeekState with that week set
   */
  def of(weekIndex: Int): WeekState = new WeekState(1L << weekIndex)

  /** Builds WeekState from week index collection.
   *
   * @param weekIndecies 0-based week indices
   * @return WeekState with those weeks set
   */
  def of(weekIndecies: Iterable[Int]): WeekState = {
    var v = 0L
    for (index <- weekIndecies) v |= (1L << index)
    new WeekState(v)
  }

  /** Builds WeekState from week index varargs. */
  def of(weekIndecies: Int*): WeekState = of(weekIndecies)

  /** Parses raw string to long bitmap (binary or IntSeg). */
  def valueOf(raw: String): Long = {
    if Strings.isBlank(raw) then 0
    else {
      val str = raw.trim()
      val chars = str.toCharArray
      val bins = Set('0', '1')
      val isBin = chars.forall(x => bins.contains(x))
      if (isBin) {
        java.lang.Long.parseLong(raw, 2)
      } else {
        var v = 0L
        val nums = IntSeg.parse(str)
        for (index <- nums) v |= (1L << index)
        v
      }
    }
  }
}

/** Assembly week in a long value.
 * week index is 1 based.
 */
@value
class WeekState(val value: Long) extends Ordered[WeekState] with Serializable {

  def this(raw: String) = {
    this(WeekState.valueOf(raw))
  }

  override def compare(other: WeekState): Int = {
    if (this.value < other.value) -1
    else if (this.value == other.value) 0
    else 1
  }

  /** Bitwise OR of week sets. */
  def |(other: WeekState): WeekState = new WeekState(this.value | other.value)

  /** Bitwise AND of week sets. */
  def &(other: WeekState): WeekState = new WeekState(this.value & other.value)

  /** Bitwise XOR of week sets. */
  def ^(other: WeekState): WeekState = new WeekState(this.value ^ other.value)

  /** Returns true if this and other share at least one week. */
  def isOverlap(other: WeekState): Boolean = (this.value & other.value) > 0

  override def toString: String = java.lang.Long.toBinaryString(value)

  /** Returns IntSeg-style digest string of selected weeks. */
  def digest: String = {
    IntSeg.digest(this.weeks)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case ws: WeekState => ws.value == this.value
      case _ => false
    }
  }

  override def hashCode: Int = java.lang.Long.hashCode(value)

  /** Returns (firstWeekIndex, lastWeekIndex) of selected weeks. */
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

  /** Index of the last selected week (0-based), or -1 if none. */
  def last: Int = {
    if value > 0 then
      val str = toString
      str.length - str.indexOf('1') - 1
    else -1
  }

  /** Index of the first selected week (0-based), or -1 if none. */
  def first: Int = {
    if value > 0 then
      val str = toString
      str.length - str.lastIndexOf('1') - 1
    else -1
  }

  /** List of 0-based week indices that are selected. */
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

  /** Returns true if the given week (0-based) is selected. */
  def contains(week: Int): Boolean = (value & (1L << week)) > 0

  /** Determines the week cycle pattern (Continuous, Odd, Even, or Random). */
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
