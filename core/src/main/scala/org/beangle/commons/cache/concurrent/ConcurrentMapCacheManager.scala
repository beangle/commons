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

  private var caches = Map.empty[String, ConcurrentMapCache[_, _]]

  override def getCache[K , V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    caches.get(name) match {
      case Some(cache) => cache.asInstanceOf[Cache[K, V]]
      case None =>
        caches.synchronized {
          caches.get(name) match {
            case Some(cache) => cache.asInstanceOf[Cache[K, V]]
            case None =>
              val newcache = new ConcurrentMapCache[K, V]
              caches += (name -> newcache)
              newcache
          }
        }
    }
  }

  override def destroy(): Unit = {
    caches = Map.empty
  }
}
