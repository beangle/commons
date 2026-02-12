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

/** Page factory and constants. */
object Page {

  /** Default 1-based page index. */
  val DefaultPageNo = 1

  /** Default page size (items per page). */
  val DefaultPageSize = 20

  /** Returns an empty page (0 items, no next/previous).
   *
   * @return empty page for type T
   */
  def empty[T](): Page[T] = EmptyPage.asInstanceOf[Page[T]]

  private object EmptyPage extends Page[Any] {

    def totalPages: Int = 0

    def pageIndex: Int = 0

    def pageSize: Int = 0

    def totalItems: Int = 0

    def hasNext: Boolean = false

    def hasPrevious: Boolean = false

    def next(): Page[Any] = this

    def previous(): Page[Any] = this

    def apply(index: Int): Any = null.asInstanceOf[Any]

    def length: Int = 0

    def moveTo(pageIndex: Int): Page[Any] = this

    def items: Seq[Any] = Nil

    def iterator: Iterator[Any] = Nil.iterator
  }
}

/** Paging model for paginated result sets.
 *
 * @author chaostone
 */
trait Page[E] extends collection.immutable.Seq[E] {

  /** Total number of pages. */
  def totalPages: Int

  /** Current page index (0-based). */
  def pageIndex: Int

  /** Items per page. */
  def pageSize: Int

  /** Total item count across all pages. */
  def totalItems: Int

  /** Next page. */
  def next(): Page[E]

  /** True if next page exists. */
  def hasNext: Boolean

  /** Previous page. */
  def previous(): Page[E]

  /** True if previous page exists. */
  def hasPrevious: Boolean

  /** Page at given index. */
  def moveTo(pageIndex: Int): Page[E]

  /** Items on this page. */
  def items: collection.Seq[E]
}
