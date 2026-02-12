/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.collection

import org.beangle.commons.bean.Properties as BeanProperties
import org.beangle.commons.lang.functor.Predicate

import java.util as ju
import scala.collection.mutable

/** Collection utilities (empty check, find, convert, bean access). */
object Collections {

  /** Null-safe check if the specified collection is empty. */
  @inline
  def isEmpty(coll: Iterable[_]): Boolean = (coll == null || coll.isEmpty)

  /** Null-safe check if the specified collection is not empty. */
  @inline
  def isNotEmpty(coll: Iterable[_]): Boolean = null != coll && !coll.isEmpty

  /** Finds the first element in candidates that exists in source.
   *
   * @param source     the collection to search in
   * @param candidates the candidates to match
   * @return the first match, or None
   */
  def findFirstMatch[T](source: Iterable[T], candidates: Iterable[T]): Option[T] = {
    val finded = if (isNotEmpty(source) && isNotEmpty(candidates))
      source match {
        case set: collection.Set[_] => candidates.find(c => set.asInstanceOf[collection.Set[T]].contains(c))
        case seq: collection.Seq[_] => candidates.find(c => seq.asInstanceOf[collection.Seq[T]].contains(c))
        case _ => None
      }
    else None
    finded
  }

  /** Splits a list into sub-lists of the specified size.
   *
   * @param list  the list to split
   * @param count the size of each sub-list
   * @return list of sub-lists
   */
  def split[T](list: List[T], count: Int): List[List[T]] = {
    val subLists = new mutable.ListBuffer[List[T]]
    if (list.size < count)
      subLists += list
    else {
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

  /** Converts a sequence to a map using the specified key property.
   *
   * @param coll        the sequence of objects
   * @param keyProperty the property name for map keys
   * @return map of key -> object
   */
  def convertToMap(coll: Seq[AnyRef], keyProperty: String): Map[_, _] =
    coll.map { obj =>
      (BeanProperties.get[Object](obj, keyProperty), obj)
    }.toMap

  /** Converts a sequence to a map using key and value properties.
   *
   * @param coll          the sequence of objects
   * @param keyProperty   the property name for map keys
   * @param valueProperty the property name for map values
   * @return map of key -> value
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

  /** Union of two collections, preserving cardinality (max freq per element).
   *
   * @param first  the first collection
   * @param second the second collection
   * @return combined list with max occurrences
   */
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

  /** Counts occurrences of each element in the collection.
   *
   * @param coll the collection
   * @return map of element -> count
   */
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

  /** Intersection preserving cardinality (min freq per element).
   *
   * @param first  the first collection
   * @param second the second collection
   * @return elements common to both with min occurrences
   */
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

  /** Removes all elements of second from first.
   *
   * @param first  the source collection
   * @param second the elements to remove
   * @return first minus second
   */
  def subtract[T](first: Iterable[T], second: Iterable[T]): List[T] = {
    val list = new mutable.ListBuffer[T]
    list ++= first
    for (t <- second) list -= t
    list.toList
  }

  /** Filters sequence by predicate.
   *
   * @param datas     the sequence to filter
   * @param predicate the filter predicate
   * @return matching elements in order
   */
  def select[T](datas: Seq[T], predicate: Predicate[T]): List[T] = {
    val rs = new mutable.ListBuffer[T]
    for (t <- datas if predicate(t)) rs += t
    rs.toList
  }

  /** Filters set by predicate.
   *
   * @param datas     the set to filter
   * @param predicate the filter predicate
   * @return matching elements
   */
  def select[T](datas: Set[T], predicate: Predicate[T]): Set[T] = {
    val rs = new mutable.HashSet[T]
    for (t <- datas if predicate.apply(t)) rs.add(t)
    rs.toSet
  }

  /** Puts all entries from the Java map into the target mutable map.
   *
   * @param target the target map to add to
   * @param origin the source Java map
   */
  def putAll[K, V, V2 <: V](target: mutable.HashMap[K, V], origin: ju.Map[K, V2]): Unit = {
    val itor = origin.entrySet.iterator
    while (itor.hasNext) {
      val entry = itor.next()
      target.put(entry.getKey, entry.getValue)
    }
  }

  /** Creates a new mutable ArrayBuffer.
   *
   * @return empty buffer
   */
  def newBuffer[T]: collection.mutable.Buffer[T] =
    new collection.mutable.ArrayBuffer[T]

  /** Creates a buffer containing the given iterable's elements.
   *
   * @param t the elements to add
   * @return a new buffer
   */
  def newBuffer[T](t: Iterable[T]): collection.mutable.Buffer[T] = {
    val buffer = new collection.mutable.ArrayBuffer[T]
    buffer ++= t
    buffer
  }

  /** Creates a buffer containing the single element.
   *
   * @param t the element
   * @return a new buffer
   */
  def newBuffer[T](t: T): collection.mutable.Buffer[T] = {
    val buffer = new collection.mutable.ArrayBuffer[T]
    buffer += t
    buffer
  }

  /** Creates a new mutable HashSet.
   *
   * @return empty set
   */
  def newSet[T]: collection.mutable.Set[T] =
    new collection.mutable.HashSet[T]

  /** Creates a new mutable HashMap.
   *
   * @return empty map
   */
  def newMap[K, V]: collection.mutable.Map[K, V] =
    new collection.mutable.HashMap[K, V]
}
