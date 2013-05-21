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
import java.util.Map

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.collection.CollectUtils

/**
 * <p>
 * ComponentType class.
 * </p>
 * 
 * @author chaostone
 * @version $Id: $
 */
class ComponentType(val componentClass:Class[_]) extends AbstractType {

  val propertyTypes :Map[String, Type] = CollectUtils.newHashMap()

  override def isComponentType = true

  override def name = componentClass.toString
 
  override def returnedClass =componentClass

  /**
   * Get the type of a particular (named) property
   */
  override def getPropertyType(propertyName:String ) :Type = {
    val t  = propertyTypes.get(propertyName)
    if (null == t) {
      val propertyType = PropertyUtils.getPropertyType(componentClass, propertyName)
      if (null != propertyType)  return new IdentifierType(propertyType)
      else t
    } else t
  }

  /**
   * Getter for the field <code>propertyTypes</code>.
   */
  def getPropertyTypes:Map[String, Type] = propertyTypes

}
