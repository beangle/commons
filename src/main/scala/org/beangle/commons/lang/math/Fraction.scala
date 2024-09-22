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

object Fraction {

  def apply(first: Int, second: Int): Fraction = {
    val a = first.toLong << 32 | second
    new Fraction(a)
  }
}

@value
class Fraction(val value: Long) extends Serializable, Ordered[Fraction] {
  override def compare(that: Fraction): Int = {
    java.lang.Long.compare(this.value, that.value)
  }

  def begin: Int = {
    (value >> 32).toInt
  }

  def end: Int = {
    (value & 0xffffffff).toInt
  }

  def length: Int = {
    (end - begin + 1)
  }

  override def toString: String = {
    s"$begin:${end}"
  }
}

object SmallFraction {

  def apply(first: Short, second: Short): SmallFraction = {
    val a = first.toInt << 16 | second
    new SmallFraction(a)
  }
}

@value
class SmallFraction(val value: Int) extends Serializable, Ordered[SmallFraction] {
  override def compare(that: SmallFraction): Int = this.value - that.value

  def begin: Short = {
    (value >> 16).toShort
  }

  def end: Short = {
    (value & 0xffff).toShort
  }

  def length: Short = {
    (end - begin + 1).asInstanceOf[Short]
  }

  override def toString: String = {
    s"$begin:${end}"
  }
}
