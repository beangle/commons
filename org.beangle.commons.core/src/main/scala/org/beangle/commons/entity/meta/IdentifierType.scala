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
 * IdentifierType class.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
class IdentifierType(val clazz:Class[_]) extends AbstractType {
  /**
   * <p>
   * getName.
   * </p>
   * 
   * @return a {@link java.lang.String} object.
   */
  override def name=clazz.toString

  /**
   * <p>
   * getReturnedClass.
   * </p>
   * 
   * @return a {@link java.lang.Class} object.
   */
  override def returnedClass = clazz

  /**
   * <p>
   * isCollectionType.
   * </p>
   * 
   * @return a boolean.
   */
  override def isCollectionType = false

  /**
   * <p>
   * isComponentType.
   * </p>
   * 
   * @return a boolean.
   */
  override def isComponentType=false

  /**
   * <p>
   * isEntityType.
   * </p>
   * 
   * @return a boolean.
   */
  override def  isEntityType =false
}
