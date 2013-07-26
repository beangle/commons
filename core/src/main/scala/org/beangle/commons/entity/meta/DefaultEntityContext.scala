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

object DefaultEntityContext {

  def buildClassEntities(entityTypes: Iterable[EntityType]): Map[String, EntityType] = {
    val builder = new collection.mutable.HashMap[String, EntityType]
    for (entityType <- entityTypes) {
      builder.put(entityType.name, entityType)
      builder.put(entityType.entityClass.getName, entityType)
    }
    builder.toMap
  }
}

/**
 * DefaultEntityContext 
 *
 * @author chaostone
 */
class DefaultEntityContext(entityTypes: Iterable[EntityType]) extends EntityContext {

  val entities = DefaultEntityContext.buildClassEntities(entityTypes)

  override def getType(clazz: Class[_]): Option[EntityType] = entities.get(clazz.getName)
}
