/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.event

import org.beangle.commons.concurrent.Locks

import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable

/** EventListener interface.
 *
 * @author chaostone
 */
trait EventListener[E <: Event] extends java.util.EventListener {

  /** Handles the event.
   *
   * @param event the event
   */
  def onEvent(event: E): Unit

  /** Returns true if this listener supports the event type.
   *
   * @param eventType the event class
   * @return true if supported
   */
  def supportsEventType(eventType: Class[_ <: Event]): Boolean

  /** Returns true if this listener supports the source type.
   *
   * @param sourceType the source class
   * @return true if supported
   */
  def supportsSourceType(sourceType: Class[_]): Boolean
}

/** EventMulticaster interface.
 */
trait EventMulticaster {

  /** Adds a listener.
   *
   * @param listener the listener to add
   */
  def addListener(listener: EventListener[_]): Unit

  /** Removes a listener.
   *
   * @param listener the listener to remove
   */
  def removeListener(listener: EventListener[_]): Unit

  /** Removes all listeners. After this call, the multicaster will perform no action on event
   * notification until new listeners are registered.
   */
  def removeAllListeners(): Unit

  /** Multicasts the event to all matching listeners.
   *
   * @param e the event
   */
  def multicast(e: Event): Unit
}

/** DefaultEventMulticaster types. */
object DefaultEventMulticaster {

  private class ListenerCacheKey(val eventType: Class[_], val sourceType: Class[_]) {

    override def equals(other: Any): Boolean = {
      val otherKey = other.asInstanceOf[ListenerCacheKey]
      (this.eventType == otherKey.eventType && this.sourceType == otherKey.sourceType)
    }

    override def hashCode(): Int =
      this.eventType.hashCode * 29 + this.sourceType.hashCode
  }
}

/** Default event multicaster implementation. */
class DefaultEventMulticaster extends EventMulticaster {

  import DefaultEventMulticaster.*

  private val lock = new ReentrantLock

  protected var listeners: List[EventListener[Event]] = Nil

  private var listenerCache: Map[ListenerCacheKey, List[EventListener[Event]]] = Map.empty

  /** Multicasts the event to all matching listeners.
   *
   * @param e the event to multicast
   */
  def multicast(e: Event): Unit = {
    val adapted = getListeners(e)
    for (listener <- adapted) listener.onEvent(e)
  }

  /** Adds a listener.
   *
   * @param listener the listener to add
   */
  def addListener(listener: EventListener[_]): Unit = {
    listeners ::= listener.asInstanceOf[EventListener[Event]]
    listenerCache = Map.empty
  }

  /** Removes a listener.
   *
   * @param listener the listener to remove
   */
  def removeListener(listener: EventListener[_]): Unit = {
    listeners = listeners diff List(listener.asInstanceOf[EventListener[Event]])
    listenerCache = Map.empty
  }

  /** Removes all listeners. */
  def removeAllListeners(): Unit = {
    listeners = Nil
    listenerCache = Map.empty
  }

  private def getListeners(e: Event): List[EventListener[Event]] = {
    val key = new ListenerCacheKey(e.getClass, e.getSource.getClass)
    var adapted = listenerCache.get(key).orNull
    if (null == adapted) {
      Locks.withLock(lock) {
        val newer = new mutable.ListBuffer[EventListener[Event]]
        for (listener <- listeners if listener.supportsEventType(e.getClass) && listener.supportsSourceType(e.getSource.getClass))
          newer += listener
        adapted = newer.toList
        listenerCache += (key -> adapted)
      }
    }
    adapted
  }
}

/** EventPublisher interface. */
trait EventPublisher {

  /** The multicaster to dispatch events to. */
  var multicaster: EventMulticaster = _

  /** Publishes the event to the multicaster.
   *
   * @param event the event to publish
   */
  def publish(event: Event): Unit = multicaster.multicast(event)
}
