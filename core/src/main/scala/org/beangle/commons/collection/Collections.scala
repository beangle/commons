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
package org.beangle.commons.collection

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.functor.Predicate
import scala.collection.mutable

object Collections {

  /**
   * 将一个集合按照固定大小查分成若干个集合。
   */
  def split[T](list: List[T], count: Int): List[List[T]] = {
    val subLists = new mutable.ListBuffer[List[T]]
    if (list.size < count) {
      subLists += list
    } else {
      var i = 0
      while (i < list.size) {
        var end = i + count
        if (end > list.size) end = list.size
        subLists += list.slice(i, end)
        i += count
      }
    }
    subLists.toList
  }

  /**
   * convertToMap.
   *
   */
  def convertToMap(coll: Seq[AnyRef], keyProperty: String): Map[_, _] = {
    val map = new mutable.HashMap[Any, Any]
    for (obj <- coll) {
      var key: Any = null
      try {
        key = PropertyUtils.getProperty[Object](obj, keyProperty)
      } catch {
        case e: Exception => Throwables.propagate(e)
      }
      map.put(key, obj)
    }
    map.toMap
  }

  /**
   * convertToMap.
   */
  def convertToMap(coll: Seq[AnyRef], keyProperty: String, valueProperty: String): Map[_, _] = {
    val map = new mutable.HashMap[Any, Any]
    for (obj <- coll) {
      val key = PropertyUtils.getProperty[AnyRef](obj, keyProperty)
      val value = PropertyUtils.getProperty[AnyRef](obj, valueProperty)
      if (null != key) map.put(key, value)
    }
    map.toMap
  }

  /**
   * Null-safe check if the specified collection is empty.
   * <p>
   * Null returns true.
   *
   * @param coll the collection to check, may be null
   * @return true if empty or null
   * @since 3.1
   */
  def isEmpty(coll: Seq[_]): Boolean = (coll == null || coll.isEmpty)

  /**
   * Null-safe check if the specified collection is not empty.
   * <p>
   * Null returns false.
   *
   * @param coll the collection to check, may be null
   * @return true if non-null and non-empty
   * @since 3.1
   */
  def isNotEmpty(coll: Seq[_]): Boolean = null != coll && !coll.isEmpty

  def union[T](first: List[T], second: List[T]): List[T] = {
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new mutable.HashSet[T]
    elts ++= first
    elts ++= second
    val list = new mutable.ListBuffer[T]
    for (obj <- elts; i <- 0 until Math.max(getFreq(obj, mapa), getFreq(obj, mapb))) list += obj
    list.toList
  }

  def getCardinalityMap[T](coll: List[T]): Map[T, Int] = {
    val count = new mutable.HashMap[T, Int]
    var it = coll.iterator
    while (it.hasNext) {
      val obj = it.next()
      count.get(obj) match {
        case Some(c) => count.put(obj, c + 1)
        case _ => count.put(obj, 1)
      }
    }
    count.toMap
  }

  private def getFreq[T](obj: T, freqMap: Map[T, Int]): Int = {
    freqMap.get(obj) match {
      case Some(count) => count
      case _ => 0
    }
  }

  def intersection[T](first: List[T], second: List[T]): List[T] = {
    val list = new mutable.ListBuffer[T]
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new mutable.HashSet[T]
    elts ++= first
    elts ++= second
    for (obj <- elts; i <- 0 until Math.min(getFreq(obj, mapa), getFreq(obj, mapb))) list += obj
    list.toList
  }

  def subtract[T](first: Seq[T], second: Seq[T]): List[T] = {
    val list = new mutable.ListBuffer[T]
    list ++= first
    for (t <- second) list -= t
    list.toList
  }

  def select[T](datas: List[T], predicate: Predicate[T]): List[T] = {
    val rs = new mutable.ListBuffer[T]
    for (t <- datas if predicate(t)) rs += t
    rs.toList
  }

  def select[T](datas: Set[T], predicate: Predicate[T]): Set[T] = {
    val rs = new mutable.HashSet[T]
    for (t <- datas if predicate.apply(t)) rs.add(t)
    rs.toSet
  }
}
