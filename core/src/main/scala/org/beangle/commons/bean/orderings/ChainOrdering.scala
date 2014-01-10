/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.bean.orderings

/**
 * 组合比较器
 *
 * @author chaostone
 */
class ChainOrdering[T](comparators: List[Ordering[T]]) extends Ordering[T] {

  /**
   * <p>
   * compare.
   * </p>
   *
   * @param first a T object.
   * @param second a T object.
   * @return 0 is equals,-1 first &lt; second ,1 first &gt; second
   */
  def compare(first: T, second: T): Int = {
    var rs = 0
    for (com <- comparators if rs != 0) rs = com.compare(first, second)
    rs
  }
}
