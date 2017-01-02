/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cdi.bind

/**
 * BindRegistry interface.
 *
 * @author chaostone
 */
trait BindRegistry {

  /**
   * getBeanNames.
   */
  def getBeanNames(clazz: Class[_]): List[String]

  /**
   * getBeanType.
   */
  def getBeanType(beanName: String): Class[_]

  /**
   * register bean definition
   * @param name beanName
   * @param clazz cannot  be null
   *
   */
  def register(name: String, clazz: Class[_]): Unit
  /**
   * register singleton
   */
  def register(name: String, obj: AnyRef): Unit
  /**
   * register bean definition
   * @param name beanName
   * @param clazz can be null if definition is abstract
   *
   */
  def register[T](name: String, clazz: Class[_], definition: T): Unit

  /**
   * contains.
   *
   */
  def contains(beanName: String): Boolean

  /**
   * bean names.
   */
  def beanNames: Set[String]

  /**
   * Whether the bean is primary
   *
   */
  def isPrimary(name: String): Boolean

  /**
   * Update primary
   */
  def setPrimary[T](name: String, isPrimary: Boolean, definition: T): Unit
}
