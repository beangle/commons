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
package org.beangle.commons.collection.page

object PagedSeq {
  private def calcMaxPageNo(pageSize: Int, total: Int): Int = {
    if (total <= pageSize) {
      1
    } else {
      val remainder = total % pageSize
      val quotient = total / pageSize
      if ((0 == remainder)) quotient else (quotient + 1)
    }
  }
}
import PagedSeq._
/**
 * <p>
 * PagedSeq class.
 * </p>
 *
 * @author chaostone
 */
class PagedSeq[E](val datas: Seq[E], limit: PageLimit) extends Page[E]() {

  var page: Page[E] = _

  var pageNo: Int = limit.pageNo - 1

  val maxPageNo: Int = calcMaxPageNo(datas.size, limit.pageSize)

  val pageSize: Int = limit.pageSize

  this.next()

  /**
   * Constructor for PagedSeq
   */
  def this(datas: Seq[E], pageSize: Int) {
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
   * getTotal.
   */
  def total: Int = datas.size

  /**
   * hasNext.
   */
  def hasNext: Boolean = pageNo < maxPageNo

  /**
   * hasPrevious.
   */
  def hasPrevious: Boolean = pageNo > 1

  /**
   * next.
   */
  def next(): Page[E] = moveTo(pageNo + 1)

  /**
   * previous.
   */
  def previous(): Page[E] = moveTo(pageNo - 1)

  /**
   *
   */
  def moveTo(pageNo: Int): Page[E] = {
    if (pageNo < 1) {
      throw new RuntimeException("error pageNo:" + pageNo)
    }
    this.pageNo = pageNo
    val toIndex = pageNo * pageSize
    val newPage = new SinglePage[E](pageNo, pageSize, datas.size, datas.slice((pageNo - 1) * pageSize,
      if ((toIndex < datas.size)) toIndex else datas.size))
    this.page = newPage
    this
  }
}
