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

import java.util.Collection
import java.util.Iterator
import java.util.List
import java.util.ListIterator
import scala.reflect.{ BeanProperty, BooleanBeanProperty }
//remove if not needed
import scala.collection.JavaConversions._

/**
 * 分页对象
 *
 * @author chaostone
 * @version $Id: $
 */
class SinglePage[E](val pageNo: Int,val pageSize: Int,val total: Int,val items: List[E]) extends Page[E] {

  def firstPageNo: Int = 1

  def maxPageNo: Int = {
    if (total < pageSize) {
      1
    } else {
      val remainder = total % pageSize
      val quotient = total / pageSize
      if ((0 == remainder)) quotient else (quotient + 1)
    }
  }

  def nextPageNo: Int = {
    if (pageNo == maxPageNo) maxPageNo
    else pageNo + 1
  }
  def previousPageNo(): Int = {
    if (pageNo == 1) pageNo
    else pageNo - 1
  }

  def contains(obj: AnyRef): Boolean = items.contains(obj)

  def containsAll(datas: Collection[_]): Boolean = items.containsAll(datas)

  def isEmpty(): Boolean = items.isEmpty

  def iterator(): Iterator[E] = items.iterator()

  def add(obj: E): Boolean = throw new RuntimeException("unsupported add")

  def addAll(datas: Collection[_ <: E]): Boolean = throw new RuntimeException("unsupported addAll")

  def clear() { throw new RuntimeException("unsupported clear") }

  def remove(obj: AnyRef): Boolean = throw new RuntimeException("unsupported removeAll")

  def removeAll(datas: Collection[_]): Boolean = throw new RuntimeException("unsupported removeAll")

  def retainAll(datas: Collection[_]): Boolean = throw new RuntimeException("unsupported retailAll")

  def size(): Int = items.size

  def toArray(): Array[AnyRef] = items.toArray()

  def toArray[T](datas: Array[T with Object]) = items.toArray[T](datas)

  def hasNext(): Boolean = maxPageNo > pageNo

  def hasPrevious(): Boolean = pageNo > 1

  def next(): Page[E] = this

  def previous(): Page[E] = this

  def moveTo(pageNo: Int): Page[E] = this

  def add(arg0: Int, arg1: E) {
    items.add(arg0, arg1)
  }

  def get(index: Int): E = items.get(index)

  def indexOf(o: AnyRef): Int = items.indexOf(o)

  def lastIndexOf(o: AnyRef): Int = items.lastIndexOf(o)

  def listIterator(): ListIterator[E] = items.listIterator()

  def listIterator(index: Int): ListIterator[E] = items.listIterator(index)

  def remove(index: Int): E = items.remove(index)

  def addAll(arg0: Int, arg1: Collection[_ <: E]): Boolean = items.addAll(arg0, arg1)

  def set(arg0: Int, arg1: E): E = items.set(arg0, arg1)

  def subList(fromIndex: Int, toIndex: Int): List[E] = items.subList(fromIndex, toIndex)
}
