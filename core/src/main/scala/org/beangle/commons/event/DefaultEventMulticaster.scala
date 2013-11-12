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

import DefaultEventMulticaster._
import scala.collection.mutable
/**
 * <p>
 * DefaultEventMulticaster class.
 * </p>
 *
 * @author chaostone
 */
class DefaultEventMulticaster extends EventMulticaster {

  protected var listeners: List[EventListener[Event]] = Nil

  private var listenerCache: Map[ListenerCacheKey, List[EventListener[Event]]] = Map.empty

  def multicast(e: Event) {
    val adapted = getListeners(e)
    for (listener <- adapted) listener.onEvent(e)
  }

  def addListener(listener: EventListener[_]) {
    listeners ::= listener.asInstanceOf[EventListener[Event]]
    listenerCache = Map.empty
  }

  def removeListener(listener: EventListener[_]) {
    listeners = listeners diff List(listener.asInstanceOf[EventListener[Event]])
    listenerCache = Map.empty
  }

  /**
   * <p>
   * removeAllListeners.
   * </p>
   */
  def removeAllListeners() {
    listeners = Nil
    listenerCache = Map.empty
  }

  protected def initListeners() {
  }

  private def getListeners(e: Event): List[EventListener[Event]] = {
    initListeners()
    val key = new ListenerCacheKey(e.getClass, e.getSource.getClass)
    var adapted = listenerCache.get(key).orNull
    if (null == adapted) {
      this.synchronized {
        if (null == adapted) {
          val newer = new mutable.ListBuffer[EventListener[Event]]
          for (listener <- listeners if listener.supportsEventType(e.getClass) && listener.supportsSourceType(e.getSource.getClass)) {
            newer += listener
          }
          adapted = newer.toList
          listenerCache += (key -> adapted)
        }
      }
    }
    adapted
  }
}
