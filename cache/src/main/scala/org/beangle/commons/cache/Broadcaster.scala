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
package org.beangle.commons.cache

import EvictMessage.{ Eviction, LocalIssuer }

/**
 * @author chaostone
 */
trait Broadcaster {
  def publishEviction(cache: String, key: Any): Unit
  def publishClear(cache: String): Unit
}

trait BroadcasterBuilder {
  def build(channel: String, localManager: CacheManager): Broadcaster
}

object EvictMessage {
  val Eviction = 0.asInstanceOf[Byte]
  val Clear = 1.asInstanceOf[Byte]
  val LocalIssuer = new scala.util.Random(System.currentTimeMillis).nextInt(1000000)
}

class EvictMessage(val cache: String, val key: Any) extends Serializable {
  import EvictMessage._
  var operation = Eviction
  var issuer: Int = LocalIssuer

  def isIssueByLocal: Boolean = {
    issuer == LocalIssuer
  }
  override def toString: String = {
    if (operation == Eviction) "clear" + cache else s"evict $key  in $cache"
  }
}
