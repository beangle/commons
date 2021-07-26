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

object PagedSeq {
  private def calcMaxPageNo(pageSize: Int, total: Int): Int =
    if (total <= pageSize)
      1
    else {
      val remainder = total % pageSize
      val quotient = total / pageSize
      if ((0 == remainder)) quotient else (quotient + 1)
    }
}
import org.beangle.commons.collection.page.PagedSeq._
/**
 * <p>
 * PagedSeq class.
 * </p>
 *
 * @author chaostone
 */
class PagedSeq[E](val datas: Seq[E], limit: PageLimit) extends Page[E]() {

  var page: Page[E] = _

  var pageIndex: Int = limit.pageIndex - 1

  val totalPages: Int = calcMaxPageNo(datas.size, limit.pageSize)

  val pageSize: Int = limit.pageSize

  this.next()

  /**
   * Constructor for PagedSeq
   */
  def this(datas: Seq[E], pageSize: Int) = {
    this(datas, PageLimit(1, pageSize))
  }

  /**
   * getItems.
   */
  def items = page.items

  /**
   * iterator.
   */
  def iterator = page.iterator

  /**
   * size
   */
  def length = page.length

  /**
   *
   */
  def apply(index: Int): E = page(index)

  /**
   * totalItems.
   */
  def totalItems: Int = datas.size

  /**
   * hasNext.
   */
  def hasNext: Boolean = pageIndex < totalPages

  /**
   * hasPrevious.
   */
  def hasPrevious: Boolean = pageIndex > 1

  /**
   * next.
   */
  def next(): Page[E] = moveTo(pageIndex + 1)

  /**
   * previous.
   */
  def previous(): Page[E] = moveTo(pageIndex - 1)

  /**
   *
   */
  def moveTo(pageIndex: Int): Page[E] = {
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
