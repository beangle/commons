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
package org.beangle.commons.collection.page

/**
 * 分页对象
 *
 * @author chaostone
 */
class SinglePage[E](val pageIndex: Int, val pageSize: Int, val totalItems: Int, val items: collection.Seq[E]) extends Page[E] {

  def totalPages: Int = {
    if (totalItems < pageSize) {
      1
    } else {
      val remainder = totalItems % pageSize
      val quotient = totalItems / pageSize
      if ((0 == remainder)) quotient else (quotient + 1)
    }
  }

  def hasNext: Boolean = totalPages > pageIndex

  def hasPrevious: Boolean = pageIndex > 1

  def next(): Page[E] = this

  def previous(): Page[E] = this

  def moveTo(pageIndex: Int): Page[E] = this

  def apply(index: Int): E = {
    items(index)
  }

  def length: Int = items.size

  def iterator: Iterator[E] = items.iterator
}
