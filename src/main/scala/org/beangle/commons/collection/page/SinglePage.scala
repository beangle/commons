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

package org.beangle.commons.collection.page

/** Single-page paging model (no cursor for next/previous).
 *
 * @author chaostone
 */
class SinglePage[E](val pageIndex: Int, val pageSize: Int, val totalItems: Int, val items: collection.Seq[E]) extends Page[E] {

  /** Total number of pages. */
  def totalPages: Int = {
    if totalItems < pageSize then 1
    else
      val remainder = totalItems % pageSize
      val quotient = totalItems / pageSize
      if (0 == remainder) quotient else quotient + 1
  }

  /** Returns true if a next page exists. */
  def hasNext: Boolean = totalPages > pageIndex

  /** Returns true if a previous page exists. */
  def hasPrevious: Boolean = pageIndex > 1

  /** Returns this (single page has no next). */
  def next(): Page[E] = this

  /** Returns this (single page has no previous). */
  def previous(): Page[E] = this

  /** Returns this (single page is fixed; ignores pageIndex).
   *
   * @param pageIndex ignored for SinglePage
   * @return this
   */
  def moveTo(pageIndex: Int): Page[E] = this

  /** Returns item at index.
   *
   * @param index the index
   * @return the item
   */
  def apply(index: Int): E = items(index)

  /** Number of items on this page. */
  def length: Int = items.size

  /** Iterator over page items. */
  def iterator: Iterator[E] = items.iterator
}
