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
package org.beangle.commons.entity.meta

/**
 * Abstract AbstractType class.
 *
 * @author chaostone
 */
abstract class AbstractType extends Type {
  /**
   * isCollectionType.
   */
  override def isCollectionType = false

  /**
   * isComponentType.
   */
  override def isComponentType = false

  /**
   * isEntityType.
   */
  override def isEntityType = false

  override def getPropertyType(property: String): Type = null

  override def equals(obj: Any) = obj match {
    case other: Type => name.equals(other.name)
    case _ => false
  }

  override def hashCode = name.hashCode

  /**
   * toString.
   */
  override def toString = name

  /**
   * newInstance.
   */
  def newInstance(): AnyRef = {
    try {
      return returnedClass.newInstance().asInstanceOf[AnyRef];
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage());
    }
  }
}
