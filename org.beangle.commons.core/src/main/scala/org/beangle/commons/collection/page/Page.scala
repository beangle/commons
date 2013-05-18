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
import Page._
//remove if not needed
import scala.collection.JavaConversions._

object Page {

  /**
   Constant <code>DEFAULT_PAGE_NUM=1</code>
   */
  val DEFAULT_PAGE_NUM = 1

  /**
   Constant <code>DEFAULT_PAGE_SIZE=20</code>
   */
  val DEFAULT_PAGE_SIZE = 20
}

/**
 * 分页对象
 *
 * @author chaostone
 * @version $Id: $
 */
trait Page[E] extends List[E] {

  /**
   * 第一页.
   *
   * @return 1
   */
  def getFirstPageNo(): Int

  /**
   * 最大页码
   *
   * @return a int.
   */
  def getMaxPageNo(): Int

  /**
   * 下一页页码
   *
   * @return a int.
   */
  def getNextPageNo(): Int

  /**
   * 上一页页码
   *
   * @return a int.
   */
  def getPreviousPageNo(): Int

  /**
   * 当前页码
   *
   * @return a int.
   */
  def getPageNo(): Int

  /**
   * 每页大小
   *
   * @return a int.
   */
  def getPageSize(): Int

  /**
   * 数据总量
   *
   * @return a int.
   */
  def getTotal(): Int

  /**
   * 下一页
   *
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def next(): Page[E]

  /**
   * 是否还有下一页
   *
   * @return a boolean.
   */
  def hasNext(): Boolean

  /**
   * 上一页
   *
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def previous(): Page[E]

  /**
   * 是否还有上一页
   *
   * @return a boolean.
   */
  def hasPrevious(): Boolean

  /**
   * 调转到指定页
   *
   * @param pageNo a int.
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def moveTo(pageNo: Int): Page[E]

  /**
   * <p>
   * getItems.
   * </p>
   *
   * @return a {@link java.util.List} object.
   */
  def getItems(): List[E]
}
