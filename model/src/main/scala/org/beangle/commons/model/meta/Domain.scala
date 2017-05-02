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

import org.beangle.commons.bean.Properties
import org.beangle.commons.model.Entity
import org.beangle.commons.collection.Collections

object Domain {

  trait MutableStructType extends StructType {
    def properties: collection.mutable.Map[String, Property]

    def getSimpleProperty(property: String): Option[Property] = {
      properties.get(property)
    }

    def getProperty(property: String): Option[Property] = {
      val idx = property.indexOf(".")
      if (idx == -1) getSimpleProperty(property)
      else getSimpleProperty(property.substring(0, idx)).asInstanceOf[MutableStructType].getProperty(property.substring(idx + 1))
    }

    def addProperty(property: Property): Unit = {
      properties.put(property.name, property)
    }
  }

  class EntityTypeImpl(val entityName: String, var clazz: Class[_]) extends MutableStructType with EntityType {
    val properties = Collections.newMap[String, Property]
    var idName: String = "id"
    def id: Property = {
      properties.get(idName).orNull
    }
    override def toString: String = {
      s"${entityName}[${clazz.getName}]"
    }
  }

  final class EmbeddableTypeImpl(val clazz: Class[_]) extends MutableStructType with EmbeddableType {
    val properties = Collections.newMap[String, Property]
    var parentName: Option[String] = None
  }

  abstract class PropertyImpl(val name: String, val clazz: Class[_]) extends Property {
    var optional: Boolean = false
  }

  final class SingularPropertyImpl(name: String, clazz: Class[_], var propertyType: Type)
    extends PropertyImpl(name, clazz) with SingularProperty

  final class CollectionPropertyImpl(name: String, clazz: Class[_], var element: Type)
      extends PropertyImpl(name, clazz) with CollectionProperty {
    var orderBy: Option[String] = None
  }

  final class MapPropertyImpl(name: String, clazz: Class[_], val key: Type, var element: Type)
    extends PropertyImpl(name, clazz) with MapProperty
}

trait Domain {

  def getEntity(clazz: Class[_]): Option[EntityType] = {
    getEntity(clazz.getName)
  }

  def getEntity(name: String): Option[EntityType]

  def entities: Map[String, EntityType]

  def newInstance[T <: Entity[_]](entityClass: Class[T]): Option[T] = {
    getEntity(entityClass) match {
      case Some(t) => Some(t.newInstance().asInstanceOf[T])
      case _       => None
    }
  }

  def newInstance[T <: Entity[ID], ID](entityClass: Class[T], id: ID): Option[T] = {
    getEntity(entityClass) match {
      case Some(t) => {
        val obj = t.newInstance()
        Properties.set(obj, t.id.name, id)
        Some(obj.asInstanceOf[T])
      }
      case _ => None
    }
  }

}

object ImmutableDomain {
  private def buildEntityMap(entities: Iterable[EntityType]): Map[String, EntityType] = {
    val builder = new collection.mutable.HashMap[String, EntityType]
    for (entity <- entities) {
      builder.put(entity.entityName, entity)
      builder.put(entity.clazz.getName, entity)
    }
    builder.toMap
  }

  def apply(entities: Iterable[EntityType]): Domain = {
    new ImmutableDomain(buildEntityMap(entities))
  }

  def empty: Domain = {
    new ImmutableDomain(Map.empty[String, EntityType])
  }
}
class ImmutableDomain(val entities: Map[String, EntityType]) extends Domain {
  override def getEntity(name: String): Option[EntityType] = {
    entities.get(name)
  }
}
