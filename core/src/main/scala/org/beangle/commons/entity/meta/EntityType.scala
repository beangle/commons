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

import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.reflect.Reflections

object EntityType {
  def extractIdType(entityClass: Class[_], idName: String): Type = {
    val clazz = Reflections.getPropertyType(entityClass, idName)
    if (null != clazz) new IdentifierType(clazz) else null
  }
}
/**
 * EntityType class.
 *
 * @author chaostone
 */
class EntityType(val entityName: String, val entityClass: Class[_], val idName: String, val idType: Type) extends AbstractType {

  Assert.notNull(idName)
  Assert.notNull(entityName)
  Assert.notNull(entityClass)

  var propertyTypes: Map[String, Type] = if (null != idType) Map((idName -> idType)) else Map()

  def this(entityName: String, entityClass: Class[_], idName: String) {
    this(entityName, entityClass, idName, EntityType.extractIdType(entityClass, idName))
  }

  def this(entityClass: Class[_]) {
    this(entityClass.getName(), entityClass, "id", EntityType.extractIdType(entityClass, "id"))
  }

  override def isEntityType = true

  /**
   * Get the type of a particular (named) property
   */
  override def getPropertyType(property: String): Type = {
    val t = propertyTypes.get(property).orNull
    if (null == t) {
      val propertyType = Reflections.getPropertyType(entityClass, property)
      if (null != propertyType) new IdentifierType(propertyType) else null
    } else t
  }

  override def name: String = entityName

  override def returnedClass = entityClass

  def addProperty(name: String, t: Type): this.type = {
    propertyTypes += (name -> t)
    this
  }
}
