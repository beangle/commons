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

import org.beangle.commons.cache.{ Broadcaster, BroadcasterBuilder, EvictMessage }
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.CacheManager
import org.beangle.commons.io.BinarySerializer
import org.beangle.commons.logging.Logging
import redis.clients.jedis.{ BinaryJedisPubSub, JedisPool }
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.util.SafeEncoder
import org.beangle.commons.cache.chain.ChainedManager

class RedisBroadcasterBuilder(pool: JedisPool, serializer: BinarySerializer) extends BroadcasterBuilder {
  def build(channel: String, localManager: CacheManager): Broadcaster = {
    if (localManager.isInstanceOf[ChainedManager]) {
      throw new RuntimeException("Local cache manager couldn't be chained.")
    }
    val broadcaster = new RedisBroadcaster(SafeEncoder.encode(channel), pool, serializer, localManager)
    broadcaster.init()
    broadcaster
  }
}

object SubscriberDaemon {
  var running = false
}
/**
 * Subscribe and on receive message thread
 */
class SubscriberDaemon(pool: JedisPool, broardcaster: RedisBroadcaster, channel: Array[Byte]) extends Runnable with Logging {
  override def run() {
    SubscriberDaemon.synchronized {
      if (SubscriberDaemon.running) {
        logger.warn("SubscriberDaemon is running,opereration aborted.")
        return
      } else {
        SubscriberDaemon.running = true
      }
    }
    var i = 0
    while (true) {
      try {
        val jedis = pool.getResource
        logger.info("Subscribing redis on channel:" + SafeEncoder.encode(channel))
        jedis.subscribe(broardcaster, channel)
        jedis.close()
      } catch {
        case e: JedisConnectionException =>
          e.printStackTrace()
          i += 1
          if (i % 5 == 0) {
            logger.error("Connect redis failed after 5 tries.")
            Thread.sleep(100000)
          }
      }
    }
  }
}
/**
 * @author chaostone
 */
class RedisBroadcaster(channel: Array[Byte], pool: JedisPool, serializer: BinarySerializer, localManager: CacheManager)
    extends BinaryJedisPubSub with Broadcaster with Initializing {

  var subscriber: Thread = _

  def init(): Unit = {
    //the subscribsion will block current thread,so we start a new one.
    subscriber = new Thread(new SubscriberDaemon(pool, this, channel))
    subscriber.setName("RedisSubscriberDaemon")
    subscriber.setDaemon(true)
    subscriber.start()
  }

  override def onMessage(channel: Array[Byte], message: Array[Byte]) {
    val msg = serializer.deserialize(message, Map.empty).asInstanceOf[EvictMessage]
    if (!msg.isIssueByLocal) {
      val cache = localManager.getCache(msg.cache, classOf[Any], classOf[Any])
      if (null != cache) {
        if (msg.operation == EvictMessage.Clear) {
          cache.clear()
        } else {
          cache.evict(msg.key)
        }
      }
    }
  }

  override def publishEviction(cache: String, key: Any): Unit = {
    val jedis = pool.getResource
    try {
      jedis.publish(channel, serializer.serialize(new EvictMessage(cache, key), Map.empty))
    } finally {
      jedis.close()
    }
  }

  override def publishClear(cache: String): Unit = {
    val jedis = pool.getResource
    try {
      jedis.publish(channel, serializer.serialize(new EvictMessage(cache, null), Map.empty))
    } finally {
      jedis.close()
    }

  }

}
