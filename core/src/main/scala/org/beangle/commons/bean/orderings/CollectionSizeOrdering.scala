/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.bean.orderings

/**
 * 比较两个集合，元素多的大
 *
 * @author chaostone
 */
class CollectionSizeOrdering[T <: Iterator[_]] extends Ordering[T] {

  /**
   * <p>
   * compare.
   * </p>
   *
   * @param first a T object.
   * @param second a T object.
   * @return equals : 0,first less then second : -1 or small , first greate then second : 1 or big
   */
  def compare(first: T, second: T): Int = {
    if (first == second) 0 else first.size - second.size
  }
}
