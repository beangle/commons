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

import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.CacheManager
import org.beangle.commons.io.BinarySerializer
import org.beangle.commons.logging.Logging
import redis.clients.util.SafeEncoder
import redis.clients.jedis.JedisPool
import org.jgroups.{ JChannel, Message, ReceiverAdapter }
import java.net.URL
import org.beangle.commons.cache.Broadcaster
import org.beangle.commons.cache.BroadcasterBuilder
import org.beangle.commons.cache.EvictMessage

class JGroupsBroadcasterBuilder(networkConfigUrl: URL, serializer: BinarySerializer) extends BroadcasterBuilder {
  def build(channel: String, localManager: CacheManager): Broadcaster = {
    val broadcaster = new JGroupsBroadcaster(channel, new JChannel(networkConfigUrl), serializer, localManager)
    broadcaster.init()
    broadcaster
  }
}

/**
 * @author chaostone
 */
class JGroupsBroadcaster(channelName: String, channel: JChannel, serializer: BinarySerializer, localManager: CacheManager)
    extends ReceiverAdapter with Broadcaster with Initializing with Logging {

  def init(): Unit = {
    channel.setReceiver(this)
    channel.connect(this.channelName)
  }

  override def receive(msg: Message) {
    if (msg.getSrc.equals(channel.getAddress)) return
    val buffer = msg.getBuffer
    if (buffer.length > 0) {
      val msg = serializer.deserialize(buffer, Map.empty).asInstanceOf[EvictMessage]
      if (!msg.isIssueByLocal) {
        if (null == msg.key) {
          localManager.getCache(msg.cache, classOf[Any], classOf[Any]).clear()
        } else {
          localManager.getCache(msg.cache, classOf[Any], classOf[Any]).evict(msg.key)
        }
      }
    }
  }

  override def publishEviction(cache: String, key: Any): Unit = {
    try {
      channel.send(new Message(null, null, serializer.serialize(new EvictMessage(cache, key), Map.empty)))
    } catch {
      case e: Throwable =>
        logger.error("Unable to evict,cache=" + cache + " key=" + key, e);
    }
  }

  override def publishClear(cache: String): Unit = {
    try {
      channel.send(new Message(null, null, serializer.serialize(new EvictMessage(cache, null), Map.empty)))
    } catch {
      case e: Throwable =>
        logger.error("Unable to clear cache :" + cache, e);
    }
  }

}