/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.event

/**
 * <p>
 * EventListener interface.
 * </p>
 *
 * @author chaostone
 */
trait EventListener[E <: Event] extends java.util.EventListener {

  /**
   * Handle an application event.
   *
   * @param event the event to respond to
   */
  def onEvent(event: E): Unit

  /**
   * Determine whether this listener actually supports the given event type.
   *
   * @param eventType a {@link java.lang.Class} object.
   * @return a boolean.
   */
  def supportsEventType(eventType: Class[_ <: Event]): Boolean

  /**
   * Determine whether this listener actually supports the given source type.
   *
   * @param sourceType a {@link java.lang.Class} object.
   * @return a boolean.
   */
  def supportsSourceType(sourceType: Class[_]): Boolean
}