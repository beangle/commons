/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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

/**
 * <p>
 * EventMulticaster interface.
 * </p>
 *
 * @author chaostone
 */
trait EventMulticaster {

  /**
   * Add a listener to be notified of all events.
   *
   * @param listener the listener to add
   */
  def addListener(listener: EventListener[_]): Unit

  /**
   * Remove a listener from the notification list.
   *
   * @param listener the listener to remove
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
   * <p>
   * multicast.
   * </p>
   *
   * @param e a {@link org.beangle.commons.event.Event} object.
   */
  def multicast(e: Event): Unit
}
