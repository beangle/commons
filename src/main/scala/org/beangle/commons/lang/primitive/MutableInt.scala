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

package org.beangle.commons.lang.primitive

/** Mutable Int wrapper with increment/decrement. */
class MutableInt(var value: Int = 0) {

  /** Increments value by 1 and returns new value. */
  def increment(): Int = {
    value += 1
    value
  }

  /** Decrements value by 1 and returns new value. */
  def decrement(): Int = {
    value -= 1
    value
  }
}
