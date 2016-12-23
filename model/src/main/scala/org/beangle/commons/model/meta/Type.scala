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
package org.beangle.commons.model.meta

/**
 * <p>
 * Type interface.
 * </p>
 *
 * @author chaostone
 */
trait Type {
  /**
   * Is this type a collection type.
   */
  def isCollectionType: Boolean

  /**
   * Is this type a component type. If so, the implementation must be castable
   * to <tt>AbstractComponentType</tt>. A component type may own collections
   * or associations and hence must provide certain extra functionality.
   */
  def isComponentType: Boolean

  /**
   * Is this type an entity type?
   */
  def isEntityType: Boolean

  /**
   * getPropertyType.
   */
  def getPropertyType(property: String): Option[Type]

  /**
   * return property type or throw NoSuchElementException
   */
  def apply(property: String): Type
  /**
   *  getName.
   */
  def name: String

  /**
   * getReturnedClass.
   */
  def returnedClass: Class[_]

  /**
   * newInstance.
   */
  def newInstance(): AnyRef
}
