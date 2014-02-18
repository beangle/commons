/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.cache.concurrent

import org.beangle.commons.cache.{Cache, CacheManager}

/**
 * Concurrent Map Cache Manager.
 *
 * @author chaostone
 * @version 1.0
 * @since 3.2.0
 */
class ConcurrentMapCacheManager extends CacheManager {

  private val caches = new collection.mutable.HashMap[String, ConcurrentMapCache[_, _]]

  override def getCache[K, V](name: String): Cache[K, V] = {
    var cache = caches.get(name).orNull
    if (cache == null) {
      caches.synchronized {
        cache = caches.get(name).orNull
        if (cache == null) {
          cache = new ConcurrentMapCache[K, V](name)
          caches.put(name, cache)
        }
      }
    }
    cache.asInstanceOf[Cache[K, V]]
  }

  override def cacheNames: Set[String] = caches.keySet.toSet
}
