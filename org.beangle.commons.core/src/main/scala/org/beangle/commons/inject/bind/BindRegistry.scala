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
 * <p>
 * BindRegistry interface.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
trait BindRegistry {

  /**
   * <p>
   * getBeanNames.
   * </p>
   *
   * @param clazz a {@link java.lang.Class} object.
   * @return a {@link java.util.List} object.
   */
  def getBeanNames(clazz: Class[_]): List[String]

  /**
   * <p>
   * getBeanType.
   * </p>
   *
   * @param beanName a {@link java.lang.String} object.
   * @return a {@link java.lang.Class} object.
   */
  def getBeanType(beanName: String): Class[_]

  /**
   * <p>
   * register.
   * </p>
   *
   * @param clazz a {@link java.lang.Class} object.
   * @param name a {@link java.lang.String} object.
   * @param args a {@link java.lang.Object} object.
   */
  def register(clazz: Class[_], name: String, args: AnyRef*): Unit

  /**
   * <p>
   * contains.
   * </p>
   *
   * @param beanName a {@link java.lang.String} object.
   * @return true if contains
   */
  def contains(beanName: String): Boolean

  /**
   * <p>
   * getBeanNames.
   * </p>
   *
   * @return bean name set
   */
  def getBeanNames(): Set[String]

  /**
   * <p>
   * Whether the bean is primary
   * </p>
   *
   * @param name
   * @return true if the bean is primary
   */
  def isPrimary(name: String): Boolean
}
