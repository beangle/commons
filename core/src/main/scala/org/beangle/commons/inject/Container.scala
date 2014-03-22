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
package org.beangle.commons.inject

/**
 * Bean Container.
 *
 * @author chaostone
 * @since 3.1.0
 */
trait Container {

  /**
   * Return true if contains
   */
  def contains(key: Any): Boolean

  /**
   * Return type of the given key.
   */
  def getType(key: Any): Option[Class[_]]

  /**
   * Return an instance
   */
  def getBean[T](key: Any): Option[T]

  /**
   * Gets an instance of the given dependency
   */
  def getBean[T](clazz: Class[T]): Option[T]

  /**
   * Return beans of the given type
   */
  def getBeans[T](clazz: Class[T]): Map[_, T]

  /**
   * Return bean keys
   */
  def keys(): Set[_]
}
