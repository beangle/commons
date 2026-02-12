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

package org.beangle.commons.cache

/** Common cache interface.
 *
 * @author chaostone
 * @since 3.2.0
 */
trait Cache[K, V] {

  /** Gets the value for the key.
   *
   * @param key the cache key
   * @return Some(value) or None
   */
  def get(key: K): Option[V]

  /** Puts a key-value pair.
   *
   * @param key   the cache key
   * @param value the value to store
   */
  def put(key: K, value: V): Unit

  /** Touches the key to restart TTL. Returns false if key does not exist.
   * Default equivalent: get(k); if empty false else remove+put.
   *
   * @param key the cache key
   * @return true if touched, false if key not exists
   */
  def touch(key: K): Boolean

  /** Returns true if the key exists.
   *
   * @param key the cache key
   * @return true if exists
   */
  def exists(key: K): Boolean

  /** Puts the value only when key is absent.
   *
   * @param key   the cache key
   * @param value the value to store
   * @return true if put succeeded (was absent)
   */
  def putIfAbsent(key: K, value: V): Boolean

  /** Replaces the value for the key.
   *
   * @param key   the cache key
   * @param value the new value
   * @return Some(old value) if existed, else None
   */
  def replace(key: K, value: V): Option[V]

  /** Replaces only when current value matches oldvalue.
   *
   * @param key      the cache key
   * @param oldvalue the expected current value
   * @param newvalue the new value
   * @return true if replaced (key and oldvalue matched)
   */
  def replace(key: K, oldvalue: V, newvalue: V): Boolean

  /** Evicts the specified key.
   *
   * @param key the cache key
   * @return true if evicted
   */
  def evict(key: K): Boolean

  /** Removes all mappings from the cache. */
  def clear(): Unit

  /** Time to live in seconds; -1 means forever. */
  def ttl: Long

  /** Time to idle in seconds; -1 means forever. */
  def tti: Long
}
