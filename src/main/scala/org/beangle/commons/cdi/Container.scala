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

package org.beangle.commons.cdi

/** Bean container for dependency injection. Manages bean lifecycle and retrieval.
 *
 * @author chaostone
 * @since 3.1.0
 */
trait Container {

  /** Returns the container id. */
  def id: String

  /** Checks whether a bean with the given key exists.
   *
   * @param key the bean key
   * @return true if the bean exists
   */
  def contains(key: String): Boolean

  /** Gets the type of the bean with the given key.
   *
   * @param key the bean key
   * @return Some(bean class) or None
   */
  def getType(key: String): Option[Class[_]]

  /** Gets the bean by key.
   *
   * @param key the bean key
   * @return Some(bean) or None
   */
  def getBean[T](key: String): Option[T]

  /** Gets the primary bean of the given type.
   *
   * @param clazz the bean class
   * @return Some(bean) or None
   */
  def getBean[T](clazz: Class[T]): Option[T]

  /** Gets all beans of the given type (name -> instance).
   *
   * @param clazz the bean class
   * @return map of bean names to instances
   */
  def getBeans[T](clazz: Class[T]): Map[String, T]

  /** Returns all bean names and their types. */
  def beanTypes: collection.Map[String, Class[_]]

  /** Closes the container and releases resources. */
  def close(): Unit

  /** Returns the underlying container implementation. */
  def underlying: AnyRef
}

trait ContainerListener {
  def onStarted(container: Container): Unit = {}
}
