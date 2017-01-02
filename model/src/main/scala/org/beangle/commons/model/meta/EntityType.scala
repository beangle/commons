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

import java.{ io => jo }
import org.beangle.commons.model.Entity
import org.beangle.commons.lang.Assert
import org.beangle.commons.bean.Properties

object EntityType {
  def extractIdType(entityClass: Class[_], idName: String): Type = {
    val clazz = Properties.getType(entityClass, idName)
    if (null != clazz) new IdentifierType(clazz) else null
  }
}
/**
 * EntityType class.
 *
 * @author chaostone
 */
class EntityType(val entityClass: Class[_], val entityName: String, val idName: String = "id") extends AbstractType {
  assert(null != idName && null != entityName && null != entityClass)

  var propertyTypes: Map[String, Type] = Map.empty

  override def isEntityType = true

  /**
   * Get the type of a particular (named) property
   */
  override def getPropertyType(property: String): Option[Type] = {
    propertyTypes.get(property)
  }

  override def apply(property: String): Type = {
    propertyTypes(property)
  }

  override def name: String = entityName

  override def returnedClass = entityClass

  def idType: Class[jo.Serializable] = propertyTypes(idName).returnedClass.asInstanceOf[Class[jo.Serializable]]
}
