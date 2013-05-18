/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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

import java.util.List
import scala.beans.BeanProperty

/**
 * <p>
 * PagedList class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class PagedList[E](val datas: List[E], limit: PageLimit) extends PageWapper[E]() {

  @BeanProperty
  var pageNo: Int = limit.getPageNo - 1

  @BeanProperty
  var maxPageNo: Int = _

  @BeanProperty
  var pageSize: Int = limit.getPageSize

  if (datas.size <= pageSize) {
    this.maxPageNo = 1
  } else {
    val remainder = datas.size % pageSize
    val quotient = datas.size / pageSize
    this.maxPageNo = if ((0 == remainder)) quotient else (quotient + 1)
  }

  this.next()

  /**
   * <p>
   * Constructor for PagedList.
   * </p>
   *
   * @param datas a {@link java.util.List} object.
   * @param pageSize a int.
   */
  def this(datas: List[E], pageSize: Int) {
    this(datas, new PageLimit(1, pageSize))
  }

  /**
   * <p>
   * getTotal.
   * </p>
   *
   * @return a int.
   */
  def getTotal(): Int = datas.size

  /**
   * <p>
   * getNextPageNo.
   * </p>
   *
   * @return a int.
   */
  def getNextPageNo(): Int = getPage.getNextPageNo

  /**
   * <p>
   * getPreviousPageNo.
   * </p>
   *
   * @return a int.
   */
  def getPreviousPageNo(): Int = getPage.getPreviousPageNo

  /**
   * <p>
   * hasNext.
   * </p>
   *
   * @return a boolean.
   */
  def hasNext(): Boolean = getPageNo < getMaxPageNo

  /**
   * <p>
   * hasPrevious.
   * </p>
   *
   * @return a boolean.
   */
  def hasPrevious(): Boolean = getPageNo > 1

  /**
   * <p>
   * next.
   * </p>
   *
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def next(): Page[E] = moveTo(pageNo + 1)

  /**
   * <p>
   * previous.
   * </p>
   *
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def previous(): Page[E] = moveTo(pageNo - 1)

  /**
   {@inheritDoc}
   */
  def moveTo(pageNo: Int): Page[E] = {
    if (pageNo < 1) {
      throw new RuntimeException("error pageNo:" + pageNo)
    }
    this.pageNo = pageNo
    val toIndex = pageNo * pageSize
    val newPage = new SinglePage[E](pageNo, pageSize, datas.size, datas.subList((pageNo - 1) * pageSize, 
      if ((toIndex < datas.size)) toIndex else datas.size))
    setPage(newPage)
    this
  }
}
