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

import org.beangle.commons.lang.annotation.value

/** Interval factory (begin-end packed as Long). */
object Interval {

  /** Creates Interval from begin and end (inclusive). */
  def apply(first: Int, second: Int): Interval = {
    val a = first.toLong << 32 | second
    new Interval(a)
  }
}

/** Integer range [begin, end] packed as Long (begin<<32|end). */
@value
class Interval(val value: Long) extends Serializable, Ordered[Interval] {
  override def compare(that: Interval): Int = {
    java.lang.Long.compare(this.value, that.value)
  }

  /** Start of range (inclusive). */
  def begin: Int = {
    (value >> 32).toInt
  }

  /** End of range (inclusive). */
  def end: Int = {
    (value & 0xffffffff).toInt
  }

  /** Number of elements in range. */
  def length: Int = {
    (end - begin + 1)
  }

  override def toString: String = {
    s"$begin-${end}"
  }
}

/** SmallInterval factory (begin-end packed as Int, Short range). */
object SmallInterval {

  /** Creates SmallInterval from begin and end (inclusive). */
  def apply(first: Short, second: Short): SmallInterval = {
    val a = first.toInt << 16 | second
    new SmallInterval(a)
  }
}

/** Short range [begin, end] packed as Int (begin<<16|end). */
@value
class SmallInterval(val value: Int) extends Serializable, Ordered[SmallInterval] {
  override def compare(that: SmallInterval): Int = this.value - that.value

  /** Start of range (inclusive). */
  def begin: Short = {
    (value >> 16).toShort
  }

  /** End of range (inclusive). */
  def end: Short = {
    (value & 0xffff).toShort
  }

  /** Number of elements in range. */
  def length: Short = {
    (end - begin + 1).asInstanceOf[Short]
  }

  override def toString: String = {
    s"$begin-${end}"
  }
}
