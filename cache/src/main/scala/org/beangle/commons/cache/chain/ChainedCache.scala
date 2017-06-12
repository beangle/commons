/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.cache.chain

import org.beangle.commons.cache.Cache

/**
 * @author chaostone
 */
class ChainedCache[K, V](first: Cache[K, V], second: Cache[K, V]) extends Cache[K, V] {

  override def get(key: K): Option[V] = {
    val firstValue = first.get(key)
    if (firstValue.isEmpty) {
      second.get(key) match {
        case None => None
        case r @ Some(value) =>
          first.put(key, value)
          r
      }
    } else {
      firstValue
    }
  }

  override def put(key: K, value: V): Unit = {
    first.put(key, value)
    second.put(key, value)
  }

  override def evict(key: K): Boolean = {
    val firstExisted = first.evict(key)
    val secondExisted = second.evict(key)
    firstExisted || secondExisted
  }

  override def touch(key: K): Boolean = {
    first.touch(key)
    second.touch(key)
  }

  override def replace(key: K, value: V): Option[V] = {
    first.replace(key, value)
    second.replace(key, value)
  }

  override def replace(key: K, oldvalue: V, newvalue: V): Boolean = {
    first.replace(key, oldvalue, newvalue)
    second.replace(key, oldvalue, newvalue)
  }

  override def exists(key: K): Boolean = {
    if (first.exists(key)) true else second.exists(key)
  }

  override def putIfAbsent(key: K, value: V): Boolean = {
    first.put(key, value)
    second.putIfAbsent(key, value)
  }

  override def clear(): Unit = {
    first.clear()
    second.clear()
  }

  override def ttl: Int = {
    first.ttl
  }

  override def tti: Int = {
    first.tti
  }
}