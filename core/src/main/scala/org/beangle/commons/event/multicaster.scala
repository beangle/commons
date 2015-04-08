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
package org.beangle.commons.event

import scala.collection.mutable
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.bean.Initializing

/**
 * EventListener interface.
 *
 * @author chaostone
 */
trait EventListener[E <: Event] extends java.util.EventListener {

  /**
   * Handle an application event.
   */
  def onEvent(event: E): Unit

  /**
   * Determine whether this listener actually supports the given event type.
   */
  def supportsEventType(eventType: Class[_ <: Event]): Boolean

  /**
   * Determine whether this listener actually supports the given source type.
   */
  def supportsSourceType(sourceType: Class[_]): Boolean
}

/**
 * EventMulticaster interface.
 */
trait EventMulticaster {

  /**
   * Add a listener to be notified of all events.
   */
  def addListener(listener: EventListener[_]): Unit

  /**
   * Remove a listener from the notification list.
   */
  def removeListener(listener: EventListener[_]): Unit

  /**
   * Remove all listeners registered with this multicaster.
   * <p>
   * After a remove call, the multicaster will perform no action on event notification until new
   * listeners are being registered.
   */
  def removeAllListeners(): Unit

  /**
   * multicast.
   */
  def multicast(e: Event): Unit
}

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
 * DefaultEventMulticaster class.
 */
class DefaultEventMulticaster extends EventMulticaster {

  import DefaultEventMulticaster._

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
   * removeAllListeners.
   */
  def removeAllListeners() {
    listeners = Nil
    listenerCache = Map.empty
  }

  private def getListeners(e: Event): List[EventListener[Event]] = {
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

/**
 * EventPublisher interface.
 */
trait EventPublisher {

  var multicaster: EventMulticaster = _

  def publish(event: Event): Unit = multicaster.multicast(event)
}

@description("依据名称查找监听者的事件广播器")
class BeanNamesEventMulticaster(listenerNames: Seq[String]) extends DefaultEventMulticaster with Initializing {

  var container: Container = _

  override def init() {
    listenerNames foreach { beanName =>
      if (container.contains(beanName)) addListener(container.getBean[EventListener[_]](beanName).get)
    }
  }
}
