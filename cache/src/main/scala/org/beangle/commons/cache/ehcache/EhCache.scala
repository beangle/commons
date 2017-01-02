/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cache.ehcache

import org.beangle.commons.cache.Cache
import org.ehcache.ValueSupplier

/**
 * @author chaostone
 */
class EhCache[K, V](cache: org.ehcache.Cache[K, V]) extends Cache[K, V] {

  override def get(key: K): Option[V] = {
    val ele = cache.get(key)
    if (null == ele) None else Some(ele)
  }

  override def touch(key: K): Boolean = {
    val ele = cache.get(key)
    if (null == ele) {
      false
    } else {
      cache.put(key, ele)
      true
    }
  }

  override def replace(key: K, value: V): Option[V] = {
    Option(cache.replace(key, value))
  }

  override def replace(key: K, oldvalue: V, newvalue: V): Boolean = {
    cache.replace(key, oldvalue, newvalue)
  }

  override def put(key: K, value: V): Unit = {
    cache.put(key, value)
  }

  override def evict(key: K): Boolean = {
    val existed = cache.containsKey(key)
    if (existed) cache.remove(key)
    existed
  }

  override def exists(key: K): Boolean = {
    cache.containsKey(key)
  }

  override def putIfAbsent(key: K, value: V): Boolean = {
    cache.putIfAbsent(key, value) == null
  }

  override def clear(): Unit = {
    cache.clear()
  }

  override def ttl: Int = {
    val duration = cache.getRuntimeConfiguration.getExpiry.getExpiryForCreation(null.asInstanceOf[K], null.asInstanceOf[V])
    duration.getTimeUnit.toSeconds(duration.getLength).asInstanceOf[Int]
  }

  override def tti: Int = {
    val duration = cache.getRuntimeConfiguration.getExpiry.getExpiryForAccess(null.asInstanceOf[K],
      null.asInstanceOf[ValueSupplier[V]])
    duration.getTimeUnit.toSeconds(duration.getLength).asInstanceOf[Int]
  }
}
