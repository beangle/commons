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

package org.beangle.commons.bean.orderings

/** Chains multiple comparators; uses first non-zero result.
 *
 * @author chaostone
 */
class ChainOrdering[T](comparators: List[Ordering[T]]) extends Ordering[T] {

  /** Compares using the chained comparators in order.
   *
   * @return 0 if equal, -1 if first &lt; second, 1 if first &gt; second
   */
  def compare(first: T, second: T): Int = {
    var rs = 0
    comparators find { com => rs = com.compare(first, second); rs != 0 }
    rs
  }
}
