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
package org.beangle.data.dao

import org.beangle.commons.collection.page._

/**
 * 基于查询的分页。<br>
 * 当使用或导出大批量数据时，使用者仍以List的方式进行迭代。<br>
 * 该实现则是内部采用分页方式。
 *
 * @author chaostone
 */
abstract class AbstractQueryPage[T](val query: LimitQuery[T]) extends Page[T] {

  var page: Page[T] = _

  var pageIndex = if (null != query.limit) query.limit.pageIndex - 1 else 0

  var totalPages = 0

  if (null == query.limit) query.limit(PageLimit(Page.DefaultPageNo, Page.DefaultPageSize))

  /**
   * 按照单个分页数据设置.
   *
   * @param page a {@link org.beangle.commons.collection.page.SinglePage} object.
   */
  protected def updatePage(page: SinglePage[T]) {
    this.page = page
    this.pageIndex = page.pageIndex
    this.totalPages = page.totalPages
  }

  def next(): Page[T] = moveTo(pageIndex + 1)

  def previous(): Page[T] = moveTo(pageIndex - 1)

  def hasNext: Boolean = totalPages > pageIndex

  def hasPrevious: Boolean = pageIndex > 1

  def pageSize: Int = query.limit.pageSize

  def totalItems: Int = page.totalItems

  def items = page.items

  def length = page.length

  def apply(index: Int): T = page(index)

  override def iterator: Iterator[T] = new PageIterator[T](this)

}

class PageIterator[T](val queryPage: AbstractQueryPage[T]) extends Iterator[T] {

  private var dataIndex: Int = 0

  private var innerIter: Iterator[T] = _

  def hasNext: Boolean = (dataIndex < queryPage.page.items.size) || queryPage.hasNext

  def next(): T = {
    if (dataIndex < queryPage.page.size) {
      dataIndex += 1
      innerIter.next
    } else {
      queryPage.next()
      dataIndex = 1
      innerIter = queryPage.page.iterator
      innerIter.next
    }
  }

  def remove() {
  }

}
