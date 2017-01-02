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

import java.lang.reflect.Array

/**
 * CollectionType class.
 *
 * @author chaostone
 */
class CollectionType(val collectionClass: Class[_], val elementType: Type) extends AbstractType {

  override def name: String = {
    val buffer = new StringBuilder()
    if (null != collectionClass)
      buffer.append(collectionClass.getName())
    buffer.append(':')
    buffer.append(elementType.name)
    return buffer.toString()
  }

  override def isCollectionType = true

  override def getPropertyType(property: String): Option[Type] = {
    Some(elementType)
  }

  override def apply(property: String): Type = {
    elementType
  }

  def returnedClass = collectionClass

  override def newInstance(): AnyRef = {
    if (collectionClass.isArray()) {
      return Array.newInstance(elementType.returnedClass, 0);
    } else {
      return super.newInstance();
    }
  }
}
