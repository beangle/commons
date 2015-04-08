/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

  val DefaultPageNo = 1

  val DefaultPageSize = 20

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

    def length(): Int = 0

    def moveTo(pageIndex: Int): Page[Any] = this

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

  def totalPages: Int

  def pageIndex: Int
  
  def pageSize: Int

  def totalItems: Int

  def next(): Page[E]

  def hasNext: Boolean

  def previous(): Page[E]
  
  def hasPrevious: Boolean

  def moveTo(pageIndex: Int): Page[E]

  def items: Seq[E]
}
