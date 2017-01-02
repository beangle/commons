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

import org.beangle.commons.model.Entity
import org.beangle.commons.bean.Properties
/**
 * <p>
 * MetadataFactory interface.
 * </p>
 *
 * @author chaostone
 */
trait EntityMetadata {
  /**
   * 根据实体名查找实体类型
   *
   * @param name a String object.
   * @return a {@link org.beangle.commons.model.metadata.Type} object.
   */
  def getType(clazz: Class[_]): Option[EntityType] = getType(clazz.getName)

  def getType(name: String): Option[EntityType]

  def newInstance[T <: Entity[_]](entityClass: Class[T]): Option[T] = {
    getType(entityClass) match {
      case Some(t) => Some(t.newInstance().asInstanceOf[T])
      case _ => None
    }
  }

  def newInstance[T <: Entity[ID], ID](entityClass: Class[T], id: ID): Option[T] = {
    getType(entityClass) match {
      case Some(t) => {
        val obj = t.newInstance()
        Properties.set(obj, t.idName, id)
        Some(obj.asInstanceOf[T])
      }
      case _ => None
    }
  }

}

object DefaultEntityMetadata {

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
 * DefaultEntityMetadata
 *
 * @author chaostone
 */
class DefaultEntityMetadata(entityTypes: Iterable[EntityType]) extends EntityMetadata {

  val entities: Map[String, EntityType] = DefaultEntityMetadata.buildClassEntities(entityTypes)

  override def getType(name: String): Option[EntityType] = entities.get(name)
}
