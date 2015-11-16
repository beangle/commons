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
package org.beangle.commons.cache

/**
 * Common interface of Cache
 *
 * @author chaostone
 * @since 3.2.0
 */
trait Cache[K <: AnyRef, V <: AnyRef] {

  /**
   * Return the cache name.
   */
  def name: String

  /**
   * Get Some(T) or None
   */
  def get(key: K): Option[V]

  /**
   * Put a new Value
   */
  def put(key: K, value: V): Unit
  /**
   * Exists key
   */
  def exists(key: K): Boolean
  /**
   * Same with put,but return true when absent
   */
  def putIfAbsent(key: K, value: V): Boolean
  /**
   * Evict specified key
   */
  def evict(key: K): Boolean

  /**
   * Return cached keys
   */
  def keys: Iterable[_]

  /**
   * Remove all mappings from the cache.
   */
  def clear(): Unit
  /**
   * Max live seconds in this cache,-1 is forever
   */
  def liveTime: Int
}
