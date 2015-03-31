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
package org.beangle.commons.collection

import java.{ util => ju }

import scala.collection.mutable

import org.beangle.commons.bean.{ Properties => BeanProperties }
import org.beangle.commons.lang.functor.Predicate

object Collections {

  /**
   * Null-safe check if the specified collection is empty.
   */
  @inline
  def isEmpty(coll: Iterable[_]): Boolean = (coll == null || coll.isEmpty)

  /**
   * Null-safe check if the specified collection is not empty.
   */
  @inline
  def isNotEmpty(coll: Iterable[_]): Boolean = null != coll && !coll.isEmpty

  def findFirstMatch[T](source: Iterable[T], candidates: Iterable[T]): Option[T] = {
    val finded = if (isNotEmpty(source) && isNotEmpty(candidates)) {
      source match {
        case set: Set[T] => candidates.find(c => set.contains(c))
        case seq: Seq[T] => candidates.find(c => seq.contains(c))
        case _ => None
      }
    } else None
    finded
  }

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
   */
  def convertToMap(coll: Seq[AnyRef], keyProperty: String): Map[_, _] = {
    coll.map { obj =>
      (BeanProperties.get[Object](obj, keyProperty), obj)
    }.toMap
  }

  /**
   * convertToMap.
   */
  def convertToMap(coll: Seq[AnyRef], keyProperty: String, valueProperty: String): Map[_, _] = {
    val map = new mutable.HashMap[Any, Any]
    coll foreach { obj =>
      val key = BeanProperties.get[AnyRef](obj, keyProperty)
      val value = BeanProperties.get[AnyRef](obj, valueProperty)
      if (null != key) map.put(key, value)
    }
    map.toMap
  }

  def union[T](first: Iterable[T], second: Iterable[T]): List[T] = {
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new mutable.HashSet[T]
    elts ++= first
    elts ++= second
    val list = new mutable.ListBuffer[T]
    for (obj <- elts; i <- 0 until Math.max(getFreq(obj, mapa), getFreq(obj, mapb))) list += obj
    list.toList
  }

  def getCardinalityMap[T](coll: Iterable[T]): Map[T, Int] = {
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

  private def getFreq[T](obj: T, freqMap: Map[T, Int]): Int = freqMap.get(obj).getOrElse(0)

  def intersection[T](first: Iterable[T], second: Iterable[T]): List[T] = {
    val list = new mutable.ListBuffer[T]
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new mutable.HashSet[T]
    elts ++= first
    elts ++= second
    for (obj <- elts; i <- 0 until Math.min(getFreq(obj, mapa), getFreq(obj, mapb))) list += obj
    list.toList
  }

  def subtract[T](first: Iterable[T], second: Iterable[T]): List[T] = {
    val list = new mutable.ListBuffer[T]
    list ++= first
    for (t <- second) list -= t
    list.toList
  }

  def select[T](datas: Seq[T], predicate: Predicate[T]): List[T] = {
    val rs = new mutable.ListBuffer[T]
    for (t <- datas if predicate(t)) rs += t
    rs.toList
  }

  def select[T](datas: Set[T], predicate: Predicate[T]): Set[T] = {
    val rs = new mutable.HashSet[T]
    for (t <- datas if predicate.apply(t)) rs.add(t)
    rs.toSet
  }

  def putAll[K, V, V2 <: V](target: mutable.HashMap[K, V], origin: ju.Map[K, V2]): Unit = {
    val itor = origin.entrySet.iterator
    while (itor.hasNext) {
      val entry = itor.next()
      target.put(entry.getKey, entry.getValue)
    }
  }

  def newBuffer[T]: collection.mutable.Buffer[T] = {
    new collection.mutable.ListBuffer[T]
  }

  def newBuffer[T](t: T*): collection.mutable.Buffer[T] = {
    val buffer = new collection.mutable.ListBuffer[T]
    buffer ++= t
    buffer
  }

  def newSet[T]: collection.mutable.Set[T] = {
    new collection.mutable.HashSet[T]
  }

  def newMap[K, V]: collection.mutable.Map[K, V] = {
    new collection.mutable.HashMap[K, V]
  }
}
