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

import java.util._
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap

import scala.collection.JavaConversions._

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Throwables
import org.beangle.commons.lang.functor.Predicate

object CollectUtils {

  /**
   * <p>
   * newArrayList.
   * </p>
   *
   * @param <E> a E object.
   * @return a {@link java.util.List} object.
   */
  def newArrayList[E](): List[E] = new ArrayList[E]()

  /**
   * <p>
   * newArrayList.
   * </p>
   *
   * @param initialCapacity a int.
   * @param <E> a E object.
   * @return a {@link java.util.List} object.
   */
  def newArrayList[E](initialCapacity: Int): List[E] = new ArrayList[E](initialCapacity)

  /**
   * <p>
   * newArrayList.
   * </p>
   *
   * @param c a {@link java.util.Collection} object.
   * @param <E> a E object.
   * @return a {@link java.util.List} object.
   */
  def newArrayList[E](c: Collection[_ <: E]): List[E] = new ArrayList[E](c)

  /**
   * <p>
   * newArrayList.
   * </p>
   *
   * @param values a E object.
   * @param <E> a E object.
   * @return a {@link java.util.List} object.
   */
  def newArrayList[E](values: E*): List[E] = {
    val list = new ArrayList[E](values.length)
    for (e <- values) list.add(e)
    list
  }

  /**
   * 将一个集合按照固定大小查分成若干个集合。
   *
   * @param list a {@link java.util.List} object.
   * @param count a int.
   * @param <T> a T object.
   * @return a {@link java.util.List} object.
   */
  def split[T](list: List[T], count: Int): List[List[T]] = {
    val subIdLists = CollectUtils.newArrayList[List[T]]
    if (list.size < count) {
      subIdLists.add(list)
    } else {
      var i = 0
      while (i < list.size) {
        var end = i + count
        if (end > list.size) end = list.size
        subIdLists.add(list.subList(i, end))
        i += count
      }
    }
    subIdLists
  }

  /**
   * <p>
   * newHashMap.
   * </p>
   *
   * @param <K> a K object.
   * @param <V> a V object.
   * @return a {@link java.util.Map} object.
   */
  def newHashMap[K, V](): Map[K, V] = new HashMap[K, V]()

  /**
   * <p>
   * newConcurrentHashMap.
   * </p>
   *
   * @param <K> a K object.
   * @param <V> a V object.
   * @return a {@link java.util.Map} object.
   */
  def newConcurrentHashMap[K, V](): ConcurrentMap[K, V] = new ConcurrentHashMap[K, V]()

  /**
   * <p>
   * newConcurrentLinkedQueue.
   * </p>
   *
   * @param <E> a E object.
   * @return a {@link java.util.Queue} object.
   */
  def newConcurrentLinkedQueue[E](): Queue[E] = new ConcurrentLinkedQueue[E]()

  /**
   * <p>
   * newHashMap.
   * </p>
   *
   * @param m a {@link java.util.Map} object.
   * @param <K> a K object.
   * @param <V> a V object.
   * @return a {@link java.util.Map} object.
   */
  def newHashMap[K, V](m: Map[_ <: K, _ <: V]): Map[K, V] = new HashMap[K, V](m)

  /**
   * <p>
   * newLinkedHashMap.
   * </p>
   *
   * @param m a {@link java.util.Map} object.
   * @param <K> a K object.
   * @param <V> a V object.
   * @return a {@link java.util.Map} object.
   */
  def newLinkedHashMap[K, V](m: Map[_ <: K, _ <: V]): Map[K, V] = new LinkedHashMap[K, V](m)

  /**
   * <p>
   * newLinkedHashMap.
   * </p>
   *
   * @param size a int.
   * @param <K> a K object.
   * @param <V> a V object.
   * @return a {@link java.util.Map} object.
   */
  def newLinkedHashMap[K, V](size: Int): Map[K, V] = new LinkedHashMap[K, V](size)

  /**
   * <p>
   * newHashSet.
   * </p>
   *
   * @param <E> a E object.
   * @return a {@link java.util.Set} object.
   */
  def newHashSet[E](): Set[E] = new HashSet[E]()

  /**
   * <p>
   * newHashSet.
   * </p>
   *
   * @param values a E object.
   * @param <E> a E object.
   * @return a {@link java.util.Set} object.
   */
  def newHashSet[E](values: E*): Set[E] = {
    val set = new HashSet[E](values.length)
    for (e <- values) {
      set.add(e)
    }
    set
  }

  /**
   * <p>
   * newHashSet.
   * </p>
   *
   * @param c a {@link java.util.Collection} object.
   * @param <E> a E object.
   * @return a {@link java.util.Set} object.
   */
  def newHashSet[E](c: Collection[_ <: E]): Set[E] = new HashSet[E](c)

  /**
   * <p>
   * convertToMap.
   * </p>
   *
   * @param coll a {@link java.util.Collection} object.
   * @param keyProperty a {@link java.lang.String} object.
   * @return a {@link java.util.Map} object.
   */
  def convertToMap(coll: Collection[_ >: AnyRef], keyProperty: String): Map[_, _] = {
    val map = newHashMap[Any, Any]
    for (obj <- coll) {
      var key: Any = null
      try {
        key = PropertyUtils.getProperty[Object](obj, keyProperty)
      } catch {
        case e: Exception => Throwables.propagate(e)
      }
      map.put(key, obj)
    }
    map
  }

  /**
   * <p>
   * convertToMap.
   * </p>
   *
   * @param coll a {@link java.util.Collection} object.
   * @param keyProperty a {@link java.lang.String} object.
   * @param valueProperty a {@link java.lang.String} object.
   * @return a {@link java.util.Map} object.
   */
  def convertToMap(coll: Collection[_], keyProperty: String, valueProperty: String): Map[_, _] = {
    val map = newHashMap[Any, Any]
    for (obj <- coll) {
      val key = PropertyUtils.getProperty[Object](obj, keyProperty)
      val value = PropertyUtils.getProperty[Object](obj, valueProperty)
      if (null != key) map.put(key, value)
    }
    map
  }

  /**
   * <p>
   * toMap.
   * </p>
   *
   * @param wordMappings an array of {@link java.lang.String} objects.
   * @return a {@link java.util.Map} object.
   */
  def toMap(wordMappings: Array[String]*): Map[String, String] = {
    val mappings = new HashMap[String, String]()
    for (i <- 0 until wordMappings.length) {
      val singular = wordMappings(i)(0)
      val plural = wordMappings(i)(1)
      mappings.put(singular, plural)
    }
    mappings
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
  def isEmpty(coll: Collection[_]): Boolean = (coll == null || coll.isEmpty)

  /**
   * Null-safe check if the specified collection is not empty.
   * <p>
   * Null returns false.
   *
   * @param coll the collection to check, may be null
   * @return true if non-null and non-empty
   * @since 3.1
   */
  def isNotEmpty(coll: Collection[_]): Boolean = null != coll && !coll.isEmpty

  def union[T](first: List[T], second: List[T]): List[T] = {
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new HashSet[T](first)
    elts.addAll(second)
    val list = newArrayList[T]
    for (obj <- elts; i <- 0 until Math.max(getFreq(obj, mapa), getFreq(obj, mapb))) list.add(obj)
    list
  }

  def getCardinalityMap[T](coll: List[T]): Map[T, Integer] = {
    val count = newHashMap[T, Integer]
    var it = coll.iterator()
    while (it.hasNext) {
      val obj = it.next()
      val c = (count.get(obj))
      if (c == null) count.put(obj, 1) else count.put(obj, new java.lang.Integer(c.intValue() + 1))
    }
    count
  }

  private def getFreq[T](obj: T, freqMap: Map[T, Integer]): Int = {
    val count = freqMap.get(obj)
    if ((count != null)) count.intValue() else 0
  }

  def intersection[T](first: List[T], second: List[T]): List[T] = {
    val list = CollectUtils.newArrayList[T]
    val mapa = getCardinalityMap(first)
    val mapb = getCardinalityMap(second)
    val elts = new HashSet[T](first)
    elts.addAll(second)
    for (obj <- elts; i <- 0 until Math.min(getFreq(obj, mapa), getFreq(obj, mapb))) list.add(obj)
    list
  }

  def subtract[T](first: List[T], second: List[T]): List[T] = {
    val list = newArrayList(first)
    for (t <- second) list.remove(t)
    list
  }

  def filter[T](datas: Collection[T], predicate: Predicate[T]) {
    var it = datas.iterator()
    while (it.hasNext) if (predicate.apply(it.next())) it.remove()
  }

  def select[T](datas: List[T], predicate: Predicate[T]): List[T] = {
    val rs = CollectUtils.newArrayList[T]
    for (t <- datas if predicate.apply(t)) rs.add(t)
    rs
  }

  def select[T](datas: Set[T], predicate: Predicate[T]): Set[T] = {
    val rs = CollectUtils.newHashSet[T]
    for (t <- datas if predicate.apply(t)) rs.add(t)
    rs
  }
}
