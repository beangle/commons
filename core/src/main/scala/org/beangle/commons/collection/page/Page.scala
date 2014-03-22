/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

object Page {

  /**
   * Constant <code>DefaultPageNo=1</code>
   */
  val DefaultPageNo = 1

  /**
   * Constant <code>DefaultPageSize=20</code>
   */
  val DefaultPageSize = 20

  def empty[T](): Page[T] = EmptyPage.asInstanceOf[Page[T]]

  private object EmptyPage extends Page[Any] {

    def maxPageNo: Int = 0

    def pageNo: Int = 0

    def pageSize: Int = 0

    def total: Int = 0

    def hasNext: Boolean = false

    def hasPrevious: Boolean = false

    def next(): Page[Any] = this

    def previous(): Page[Any] = this

    def apply(index: Int): Any = null.asInstanceOf[Any]

    def length(): Int = 0

    def moveTo(pageNo: Int): Page[Any] = this

    def items: Seq[Any] = Nil

    def iterator = Nil.iterator
  }
}

import Page._
/**
 * 分页对象
 *
 * @author chaostone
 */
trait Page[E] extends Seq[E] {

  /**
   * 最大页码
   *
   * @return a int.
   */
  def maxPageNo: Int

  /**
   * 当前页码
   *
   * @return a int.
   */
  def pageNo: Int
  /**
   * 每页大小
   *
   * @return a int.
   */
  def pageSize: Int

  /**
   * 数据总量
   *
   * @return a int.
   */
  def total: Int

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
  def hasNext: Boolean

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
  def hasPrevious: Boolean

  /**
   * 调转到指定页
   *
   * @param pageNo a int.
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def moveTo(pageNo: Int): Page[E]

  /**
   * getItems.
   */
  def items: Seq[E]
}
