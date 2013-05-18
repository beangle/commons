/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.event

import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import DefaultEventMulticaster._
//remove if not needed
import scala.collection.JavaConversions._

object DefaultEventMulticaster {

  private class ListenerCacheKey(val eventType: Class[_], val sourceType: Class[_]) {

    override def equals(other: Any): Boolean = {
      val otherKey = other.asInstanceOf[ListenerCacheKey]
      (this.eventType == otherKey.eventType && this.sourceType == otherKey.sourceType)
    }

    override def hashCode(): Int = {
      this.eventType.hashCode * 29 + this.sourceType.hashCode
    }
  }
}

/**
 * <p>
 * DefaultEventMulticaster class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class DefaultEventMulticaster extends EventMulticaster {

  protected var listeners: List[EventListener[Event]] = CollectUtils.newArrayList()

  private var listenerCache: Map[ListenerCacheKey, List[EventListener[Event]]] = CollectUtils.newConcurrentHashMap()

  def multicast(e: Event) {
    val adapted = getListeners(e)
    for (listener <- adapted) listener.onEvent(e)
  }

  def addListener(listener: EventListener[_]) {
    this.synchronized {
      listeners.add(listener.asInstanceOf[EventListener[Event]])
      listenerCache.clear()
    }
  }
  
  def removeListener(listener: EventListener[_]) {
    this.synchronized {
      listeners.remove(listener)
      listenerCache.clear()
    }
  }

  /**
   * <p>
   * removeAllListeners.
   * </p>
   */
  def removeAllListeners() {
    this.synchronized {
      listeners.clear()
      listenerCache.clear()
    }
  }

  protected def initListeners() {
  }

  private def getListeners(e: Event): List[EventListener[Event]] = {
    initListeners()
    val key = new ListenerCacheKey(e.getClass, e.getSource.getClass)
    var adapted = listenerCache.get(key)
    if (null == adapted) {
      this.synchronized {
        if (null == adapted) {
          adapted = CollectUtils.newArrayList()
          for (listener <- listeners if listener.supportsEventType(e.getClass) && listener.supportsSourceType(e.getSource.getClass)) {
            adapted.add(listener)
          }
          listenerCache.put(key, adapted)
        }
      }
    }
    adapted
  }
}
