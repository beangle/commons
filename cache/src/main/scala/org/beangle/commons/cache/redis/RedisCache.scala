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
package org.beangle.commons.cache.redis

import scala.collection.JavaConverters.asScalaBuffer

import org.beangle.commons.cache.Cache
import org.beangle.commons.io.BinarySerializer

import RedisCache.buildKey
import redis.clients.jedis.JedisPool

object RedisCache {

  def buildKey(name: String, key: Any): String = {
    key match {
      case n: Number       => name + ":I:" + n
      case s: CharSequence => name + ":S:" + s
      case o: Any          => name + ":O:" + o
    }
  }
}

/**
 * @author chaostone
 */
class RedisCache[K, V](name: String, pool: JedisPool, serializer: BinarySerializer, val ttl: Int = -1)
    extends Cache[K, V] {

  import RedisCache._

  private val NX = "NX".getBytes
  private val EX = "EX".getBytes

  override def get(key: K): Option[V] = {
    val cache = pool.getResource
    try {
      val b = cache.get(buildKey(name, key).getBytes)
      if (b == null) None else Some(serializer.deserialize(b, Map.empty).asInstanceOf[V])
    } finally {
      cache.close()
    }
  }

  override def put(key: K, value: V): Unit = {
    val cache = pool.getResource
    try {
      val redisKey = buildKey(name, key).getBytes
      if (ttl > 0) {
        cache.setex(redisKey, ttl, serializer.serialize(value, Map.empty))
      } else {
        cache.set(redisKey, serializer.serialize(value, Map.empty))
      }
    } finally {
      cache.close()
    }
  }

  override def touch(key: K): Boolean = {
    val cache = pool.getResource
    try {
      cache.expire(buildKey(name, key).getBytes, ttl) > 0
    } finally {
      cache.close()
    }
  }

  def replace(key: K, value: V): Option[V] = {
    val cache = pool.getResource
    try {
      val redisKey = buildKey(name, key).getBytes
      val o = cache.get(redisKey)
      val newValue = serializer.serialize(value, Map.empty)
      if (ttl > 0) {
        cache.setex(redisKey, ttl, serializer.serialize(value, Map.empty))
      } else {
        cache.set(redisKey, serializer.serialize(value, Map.empty))
      }
      if (o == null) None else Some(serializer.deserialize(o, Map.empty).asInstanceOf[V])
    } finally {
      cache.close()
    }
  }

  def replace(key: K, oldvalue: V, newvalue: V): Boolean = {
    val cache = pool.getResource
    try {
      val redisKey = buildKey(name, key).getBytes
      val o = cache.get(redisKey)
      if (o != null && o == serializer.serialize(oldvalue, Map.empty)) {
        val newValue = serializer.serialize(newvalue, Map.empty)
        if (ttl > 0) {
          cache.setex(redisKey, ttl, serializer.serialize(newvalue, Map.empty))
        } else {
          cache.set(redisKey, serializer.serialize(newvalue, Map.empty))
        }
        true
      } else {
        false
      }
    } finally {
      cache.close()
    }
  }

  override def exists(key: K): Boolean = {
    val cache = pool.getResource
    try {
      cache.exists(buildKey(name, key).getBytes)
    } finally {
      cache.close()
    }
  }

  override def putIfAbsent(key: K, value: V): Boolean = {
    val cache = pool.getResource
    try {
      val redisKey = buildKey(name, key).getBytes
      if (ttl > 0) {
        cache.set(redisKey, serializer.serialize(value, Map.empty), NX, EX, ttl) == "OK"
      } else {
        cache.set(redisKey, serializer.serialize(value, Map.empty), NX) == "OK"
      }
      false
    } finally {
      cache.close()
    }
  }

  override def evict(key: K): Boolean = {
    val cache = pool.getResource
    try {
      cache.del(buildKey(name, key)) > 0
    } finally {
      cache.close()
    }
  }

  override def clear(): Unit = {
    val cache = pool.getResource
    try {
      val keys = cache.keys(name + ":*").asInstanceOf[java.util.List[_]]
      cache.del(keys.toArray.asInstanceOf[Array[String]]: _*)
    } finally {
      cache.close()
    }
  }

  override def tti: Int = {
    ttl
  }
}
