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
package org.beangle.commons.cache.redis

import org.beangle.commons.cache.AbstractCacheManager
import org.beangle.commons.cache.Cache
import org.beangle.commons.io.BinarySerializer

import redis.clients.jedis.JedisPool

/**
 * @author chaostone
 */
class RedisCacheManager(pool: JedisPool, serializer: BinarySerializer, autoCreate: Boolean = true)
    extends AbstractCacheManager(autoCreate) {

  var ttl: Int = -1

  protected override def newCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    new RedisCache(name, pool, serializer, ttl)
  }

  protected override def findCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    new RedisCache(name, pool, serializer, ttl)
  }

  override def destroy(): Unit = {
    pool.destroy()
  }
}
