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
import scala.reflect.ClassTag

/**
 * <p>
 * Abstract PageWapper class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
abstract class PageWapper[E] extends Page[E] {

  var page: Page[E] = _

  /**
   * <p>
   * getFirstPageNo.
   * </p>
   *
   * @return a int.
   */
  def firstPageNo: Int = 1

  /**
   * <p>
   * getItems.
   * </p>
   *
   * @return a {@link java.util.List} object.
   */
  def items: List[E] = page.items

  /**
   * <p>
   * iterator.
   * </p>
   *
   * @return a {@link java.util.Iterator} object.
   */
  def iterator(): Iterator[E] = page.iterator()

  /**
   * <p>
   * add.
   * </p>
   *
   * @param obj a E object.
   * @return a boolean.
   */
  def add(obj: E): Boolean = page.add(obj)

  /**
   *
   */
  def addAll(datas: Collection[_ <: E]): Boolean = page.addAll(datas)

  /**
   * <p>
   * clear.
   * </p>
   */
  def clear() {
    page.clear()
  }

  /**
   *
   */
  def contains(obj: AnyRef): Boolean = page.contains(obj)

  /**
   *
   */
  def containsAll(datas: Collection[_]): Boolean = page.containsAll(datas)

  /**
   * <p>
   * isEmpty.
   * </p>
   *
   * @return a boolean.
   */
  def isEmpty: Boolean = page.isEmpty

  /**
   * <p>
   * size.
   * </p>
   *
   * @return a int.
   */
  def size: Int = page.size

  /**
   * <p>
   * toArray.
   * </p>
   *
   * @return an array of {@link java.lang.Object} objects.
   */
  override def toArray()= page.toArray()

  /**
   * <p>
   * toArray.
   * </p>
   *
   * @param datas an array of T objects.
   * @param <T> a T object.
   * @return an array of T objects.
   */
  override def toArray[T](datas: Array[T with Object]) = page.toArray[T](datas)
  /**
   *
   */
  def remove(obj: AnyRef): Boolean = page.remove(obj)

  /**
   *
   */
  def removeAll(datas: Collection[_]): Boolean = page.removeAll(datas)

  /**
   *
   */
  def retainAll(datas: Collection[_]): Boolean = page.retainAll(datas)

  /**
   * <p>
   * add.
   * </p>
   *
   * @param index a int.
   * @param arg1 a E object.
   */
  def add(index: Int, arg1: E) {
    page.add(index, arg1)
  }

  /**
   *
   */
  def addAll(index: Int, arg1: Collection[_ <: E]): Boolean = page.addAll(index, arg1)

  /**
   *
   */
  def get(index: Int): E = page.get(index)

  /**
   *
   */
  def lastIndexOf(o: AnyRef): Int = page.lastIndexOf(o)

  /**
   * <p>
   * listIterator.
   * </p>
   *
   * @return a {@link java.util.ListIterator} object.
   */
  def listIterator(): ListIterator[E] = page.listIterator()

  /**
   *
   */
  def listIterator(index: Int): ListIterator[E] = page.listIterator(index)

  /**
   * <p>
   * remove.
   * </p>
   *
   * @param index a int.
   * @return a E object.
   */
  def remove(index: Int): E = page.remove(index)

  /**
   * <p>
   * set.
   * </p>
   *
   * @param index a int.
   * @param arg1 a E object.
   * @return a E object.
   */
  def set(index: Int, arg1: E): E = page.set(index, arg1)

  /**
   *
   */
  def subList(fromIndex: Int, toIndex: Int): List[E] = page.subList(fromIndex, toIndex)

  /**
   *
   */
  def indexOf(o: AnyRef): Int = page.indexOf(o)
}
