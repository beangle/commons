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

import java.util.AbstractList
import java.util.ArrayList
import java.util.List
//remove if not needed
import scala.collection.JavaConversions._

object Pages {

  /**
   * <p>
   * emptyPage.
   * </p>
   *
   * @param <T> a T object.
   * @return a {@link org.beangle.commons.collection.page.Page} object.
   */
  def emptyPage[T](): Page[T] = EmptyPage.asInstanceOf[Page[T]]

}

object EmptyPage extends AbstractList[Any] with Page[Any] {

  def getFirstPageNo(): Int = 0

  def getMaxPageNo(): Int = 0

  def getNextPageNo(): Int = 0

  def getPageNo(): Int = 0

  def getPageSize(): Int = 0

  def getPreviousPageNo(): Int = 0

  def getTotal(): Int = 0

  def hasNext(): Boolean = false

  def hasPrevious(): Boolean = false

  def next(): Page[Any] = this

  def previous(): Page[Any] = this

  def get(index: Int): Any = null.asInstanceOf[Any]

  def size(): Int = 0

  def moveTo(pageNo: Int): Page[Any] = this

  def getItems(): List[Any] = new ArrayList[Any](0)
}
