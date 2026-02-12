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

package org.beangle.commons.lang.math

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Numbers, Strings}

/** Integer range segment assembly (e.g. 1-10, [2-10]even). */
object IntSeg {

  /** Assembles a number sequence into range segments (e.g. 1-10, [2-10]even, [4-12]).
   *
   * @param ns the input number sequence
   * @return the assembled IntRange segments; empty if ns is null or empty
   */
  def assemble(ns: Iterable[Int]): Iterable[IntRange] = {
    if (ns == null || ns.isEmpty) {
      return List.empty
    }
    val nums = ns.toArray
    java.util.Arrays.sort(nums)
    val patterns = Collections.newBuffer[IntRange]
    var last = new IntRange(nums(0), nums(0), 1)
    patterns.addOne(last)
    for (i <- 1 until nums.length) {
      val number: Int = nums(i)
      if (!last.accept(number)) {
        val next = last.next(number)
        if (last.start == next.start) {
          last.copy(next)
        } else {
          last = next
          patterns.addOne(last)
        }
      }
    }
    patterns
  }

  /** Formats number sequence as range string (e.g. "1-10 12-20单").
   *
   * @param nums     number sequence
   * @param strigula range separator (default "-")
   * @param sep      segment separator (default " ")
   * @return formatted string
   */
  def digest(nums: Iterable[Int], strigula: String = "-", sep: String = " "): String = {
    val seqs = IntSeg.assemble(nums).map { s =>
      if (s.step == 1) {
        if (s.start == s.end) s.start.toString
        else s"${s.start}${strigula}${s.end}"
      } else if (s.step == 2) {
        if (s.start % 2 == 1) s"${s.start}${strigula}${s.end}单"
        else s"${s.start}${strigula}${s.end}双"
      } else {
        s.toString
      }
    }
    seqs.mkString(sep)
  }

  private def normalize(seq: String): String = {
    var s = seq
    s = Strings.replace(s, "，", ",")
    s = Strings.replace(s, "－", "-")
    s = Strings.replace(s, "—", "-")
    s = Strings.replace(s, "~", "-")
    s
  }

  /** Parses range string (e.g. "1-10,12-20单,5") to Int sequence.
   *
   * @param str range string (supports 单/双, comma, hyphen)
   * @return flat list of integers
   */
  def parse(str: String): Iterable[Int] = {
    val newstr = normalize(str)
    val pairs = Strings.split(newstr, ",")

    val numbers = Collections.newBuffer[Int]
    for (pair <- pairs) {
      if (Strings.contains(pair, "-")) {
        var step = 1
        if (pair.indexOf('单') != -1) step = 2
        else if (pair.indexOf('双') != -1) step = 2
        val p = pair.replaceAll("[^\\d-]", "")
        val start = Strings.substringBefore(p, "-")
        val end = Strings.substringAfter(p, "-")
        if (Numbers.isDigits(start) && Numbers.isDigits(end)) {
          val s = Numbers.toInt(start)
          val e = Numbers.toInt(end)
          if (s <= e) {
            numbers.addAll(IntRange(s, e, step).nums)
          }
        }
      }
      else if (Numbers.isDigits(pair)) numbers.addOne(Numbers.toInt(pair))
    }
    numbers
  }

  /** Integer range [start, end] with step (1=consecutive, 2=odd/even). */
  class IntRange(var start: Int, var end: Int, var step: Int) {
    override def toString: String = {
      s"[$start,$end]($step)"
    }

    /** Tests whether i extends this range (consecutive or step pattern); if so updates end.
     *
     * @param i candidate integer
     * @return true if i is accepted and range extended
     */
    def accept(i: Int): Boolean = {
      val matched =
        if i == this.end then true
        else if i - this.end == step then true
        else false

      if matched then this.end = i
      matched
    }

    private def continuous(i: Int, s: Int = 1): IntRange = {
      new IntRange(i, i, s)
    }

    /** Copies start, end, step from that to this. */
    def copy(that: IntRange): Unit = {
      this.start = that.start
      this.end = that.end
      this.step = that.step
    }

    /** Returns a new IntRange that would accept number (consecutive or odd/even).
     *
     * @param number next number in sequence
     * @return new IntRange
     */
    def next(number: Int): IntRange = {
      if (this.step == 2) {
        return continuous(number)
      }
      // current pattern is consecutive; return new consecutive pattern
      if (!(this.end == this.start)) {
        return continuous(number)
      }
      // start==end and consecutive; try odd/even pattern
      val next = continuous(this.start, 2)
      if next.accept(number) then next
      else continuous(number)
    }

    /** Returns the integer sequence for this range. */
    def nums: Seq[Int] = {
      Range(start, end + 1, step)
    }
  }
}
