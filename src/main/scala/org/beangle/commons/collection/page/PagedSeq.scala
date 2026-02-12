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

import org.beangle.commons.collection.page.PagedSeq.*

/** PagedSeq factory and helpers. */
object PagedSeq {

  /** Computes max page number from total items and page size. */
  private def calcMaxPageNo(pageSize: Int, total: Int): Int = {
    if total <= pageSize then 1
    else
      val remainder = total % pageSize
      val quotient = total / pageSize
      if ((0 == remainder)) quotient else (quotient + 1)
  }
}

/** Paged sequence with page navigation.
 *
 * @author chaostone
 */
class PagedSeq[E](val datas: Seq[E], limit: PageLimit) extends Page[E]() {

  /** Current page slice. */
  var page: Page[E] = _

  /** Current page index (0-based). */
  var pageIndex: Int = limit.pageIndex - 1

  /** Total number of pages. */
  val totalPages: Int = calcMaxPageNo(datas.size, limit.pageSize)

  /** Items per page. */
  val pageSize: Int = limit.pageSize

  this.next()

  /** Alternate constructor with page size only (page index 1). */
  def this(datas: Seq[E], pageSize: Int) = {
    this(datas, PageLimit(1, pageSize))
  }

  /** Returns items on the current page. */
  override def items: collection.Seq[E] = page.items

  /** Returns iterator over current page items. */
  override def iterator: collection.Iterator[E] = page.iterator

  /** Returns the number of items on the current page. */
  override def length: Int = page.length

  /** Returns item at index within current page.
   *
   * @param index the index in current page
   * @return the item
   */
  override def apply(index: Int): E = page(index)

  /** Total number of items across all pages. */
  override def totalItems: Int = datas.size

  /** Returns true if there is a next page. */
  override def hasNext: Boolean = pageIndex < totalPages

  /** Returns true if there is a previous page. */
  override def hasPrevious: Boolean = pageIndex > 1

  /** Advances to next page. */
  override def next(): Page[E] = moveTo(pageIndex + 1)

  /** Goes to previous page. */
  override def previous(): Page[E] = moveTo(pageIndex - 1)

  /** Moves to given page index (1-based).
   *
   * @param pageIndex the page number
   * @return this
   */
  override def moveTo(pageIndex: Int): Page[E] = {
    if (pageIndex < 1) throw new RuntimeException("error pageIndex:" + pageIndex)
    this.pageIndex = pageIndex
    val toIndex = pageIndex * pageSize
    val newPage = new SinglePage[E](pageIndex, pageSize, datas.size, datas.slice(
      (pageIndex - 1) * pageSize,
      if ((toIndex < datas.size)) toIndex else datas.size))
    this.page = newPage
    this
  }
}
