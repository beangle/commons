/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.lang.tuple

import org.beangle.commons.lang.Objects

object Pair {

  /**
   * Obtains an immutable pair of from two objects inferring the generic types.
   * This factory allows the pair to be created using inference to obtain the generic types.
   *
   * @param <L> the left element type
   * @param <R> the right element type
   * @param left the left element, may be null
   * @param right the right element, may be null
   * @return a pair formed from the two parameters, not null
   */
  def of[L, R](left: L, right: R): Pair[L, R] = new Pair[L, R](left, right)
}

/**
 * A immutable pair consisting of two elements.
 *
 * @author chaostone
 * @param <L> the left element type
 * @param <R> the right element type
 */
@SerialVersionUID(-7643900124010501814L)
class Pair[+L, +R](left: L, right: R) extends Tuple2[L, R](left,right){

  override def hashCode(): Int = {
    (if (_1 == null) 0 else _1.hashCode) ^ (if (_2 == null) 0 else _2.hashCode)
  }

  def left = _1

  def right = _2
}
