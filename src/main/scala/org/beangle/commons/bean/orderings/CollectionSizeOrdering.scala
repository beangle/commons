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

/** Orders collections by size; larger collections rank higher.
 *
 * @author chaostone
 */
class CollectionSizeOrdering[T <: Iterator[_]] extends Ordering[T] {

  /** Compares by element count.
   *
   * @param first  the first collection
   * @param second the second collection
   * @return 0 if equal, negative if first smaller, positive if first larger
   */
  def compare(first: T, second: T): Int =
    if (first sameElements second) 0 else first.size - second.size
}
