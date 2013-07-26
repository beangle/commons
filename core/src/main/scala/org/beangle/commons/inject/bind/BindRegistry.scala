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
package org.beangle.commons.inject.bind

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
   * register.
   *
   */
  def register(clazz: Class[_], name: String, args: Any*): Unit

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
}
