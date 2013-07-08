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

import java.lang.reflect.Array

/**
 * <p>
 * CollectionType class.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
class CollectionType(val elementType:Type) extends AbstractType {

  var collectionClass:Class[_]=_

  var indexClass:Class[_]=_

  var array = false

  override def name:String = {
    val buffer = new StringBuilder()
    if (null != collectionClass) {
      buffer.append(collectionClass.getName())
    }
    buffer.append(':')
    if (null != indexClass) {
      buffer.append(indexClass.getName())
    }
    buffer.append(':')
    buffer.append(elementType.name)
    return buffer.toString()
  }

  override def isCollectionType=true

  override def getPropertyType(property:String ):Type = elementType

  /**
   * Is this collection indexed?
   * 
   * @return a boolean.
   */
  def hasIndex:Boolean=(null != indexClass) && (indexClass.equals(classOf[Int]))

  def returnedClass = collectionClass

  override def newInstance():AnyRef= {
    if (array) {
      return Array.newInstance(elementType.returnedClass, 0);
    } else {
      return super.newInstance();
    }
  }
}
