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
package org.beangle.commons.entity.meta

/**
 * <p>
 * Type interface.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
trait Type {
  /**
   * Is this type a collection type.
   * 
   * @return a boolean.
   */
  def isCollectionType:Boolean

  /**
   * Is this type a component type. If so, the implementation must be castable
   * to <tt>AbstractComponentType</tt>. A component type may own collections
   * or associations and hence must provide certain extra functionality.
   * 
   * @return boolean
   */
  def isComponentType:Boolean

  /**
   * Is this type an entity type?
   * 
   * @return boolean
   */
  def isEntityType:Boolean

  /**
   * <p>
   * getPropertyType.
   * </p>
   * 
   * @param property a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.entity.metadata.Type} object.
   */
  def getPropertyType(property: String):Type

  /**
   * <p>
   * getName.
   * </p>
   * 
   * @return a {@link java.lang.String} object.
   */
  def name:  String 

  /**
   * <p>
   * getReturnedClass.
   * </p>
   * 
   * @return a {@link java.lang.Class} object.
   */
  def returnedClass:Class[_]

  /**
   * <p>
   * newInstance.
   * </p>
   * 
   * @return a {@link java.lang.Object} object.
   */
  def newInstance():AnyRef
}
