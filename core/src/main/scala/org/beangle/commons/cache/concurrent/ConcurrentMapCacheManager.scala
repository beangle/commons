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

import org.beangle.commons.cache.{ Cache, CacheManager }

/**
 * Concurrent Map Cache Manager.
 *
 * @author chaostone
 * @version 1.0
 * @since 3.2.0
 */
class ConcurrentMapCacheManager(val name: String = "concurrent") extends CacheManager {

  private val caches = new java.util.concurrent.ConcurrentHashMap[String, ConcurrentMapCache[_, _]]

  override def getCache[K <: AnyRef, V <: AnyRef](name: String): Cache[K, V] = {
    val cache = caches.get(name)
    if (cache == null) {
      caches.synchronized {
        val cache = caches.get(name)
        if (cache == null) {
          val newcache = new ConcurrentMapCache[K, V](name)
          caches.put(name, newcache)
          newcache
        } else {
          cache.asInstanceOf[Cache[K, V]]
        }
      }
    } else {
      cache.asInstanceOf[Cache[K, V]]
    }
  }

  override def cacheNames: Set[String] = {
    import collection.JavaConversions._
    caches.keySet.toSet
  }

  override def destroy(): Unit = {
    caches.clear()
  }
}
