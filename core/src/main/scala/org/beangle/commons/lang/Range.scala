/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

package org.beangle.commons.lang

@deprecated("use scala's Range when using scala 2.10.4")
final object Range {
  @deprecated def range(start: Int, end: Int): IntRange = new IntRange(start, end)
  @deprecated def range(start: Int, end: Int, step: Int): IntStepRange = new IntStepRange(start, end, step)
}

final class IntStepRange( final val start: Int, val end: Int, val step: Int) {

  @inline def foreach[@specialized(Unit) U](f: Int => U) {
    var i = start
    val term = end
    val s = step
    while (i < term) {
      f(i)
      i += s
    }
  }
}

final class IntRange(val start: Int, val end: Int) {
  @inline def foreach[@specialized(Unit) U](f: Int => U) {
    var s = start
    val e = end
    while (s < e) {
      f(s)
      s += 1
    }
  }
}