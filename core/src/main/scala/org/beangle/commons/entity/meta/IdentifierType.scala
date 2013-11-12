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
 * IdentifierType class.
 *
 * @author chaostone
 */
class IdentifierType(val clazz: Class[_]) extends AbstractType {
  /**
   * getName.
   *
   * @return a {@link java.lang.String} object.
   */
  override def name = clazz.toString

  /**
   * getReturnedClass.
   *
   * @return a {@link java.lang.Class} object.
   */
  override def returnedClass = clazz

  /**
   * isCollectionType.
   *
   * @return a boolean.
   */
  override def isCollectionType = false

  /**
   * isComponentType.
   *
   * @return a boolean.
   */
  override def isComponentType = false

  /**
   * isEntityType.
   *
   * @return a boolean.
   */
  override def isEntityType = false
}
