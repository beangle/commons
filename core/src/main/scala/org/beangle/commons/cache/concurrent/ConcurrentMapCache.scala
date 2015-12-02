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
package org.beangle.commons.cache.concurrent

import java.util.concurrent.ConcurrentMap
import org.beangle.commons.cache.Cache

/**
 * Cache based Concurrent Map.
 *
 * @author chaostone
 * @since 3.2.0
 */
class ConcurrentMapCache[K, V] extends Cache[K, V] {

  private val store = new collection.concurrent.TrieMap[K, V]

  override def get(key: K): Option[V] = store.get(key)

  override def put(key: K, value: V) {
    store.put(key, value)
  }

  override def evict(key: K): Boolean = {
    !store.remove(key).isEmpty
  }

  override def exists(key: K): Boolean = {
    store.contains(key)
  }

  override def putIfAbsent(key: K, value: V): Boolean = {
    store.putIfAbsent(key, value).isEmpty
  }

  override def replace(key: K, value: V): Option[V] = {
    store.replace(key, value)
  }

  override def replace(key: K, oldvalue: V, newvalue: V): Boolean = {
    store.replace(key, oldvalue, newvalue)
  }

  override def clear(): Unit = {
    store.clear()
  }

  override def ttl: Int = {
    -1
  }

  override def tti: Int = {
    -1
  }

  def touch(key: K): Boolean = {
    store.contains(key)
  }
}
