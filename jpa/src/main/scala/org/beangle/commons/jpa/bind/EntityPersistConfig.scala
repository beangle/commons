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
package org.beangle.commons.jpa.bind

import javax.persistence.Entity

import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.commons.jpa.util.Jpas

object EntityPersistConfig {

  final class CollectionDefinition(val clazz: Class[_], val property: String) {
    var cacheRegion: String = _
    var cacheUsage: String = _

    def cache(region: String, usage: String): this.type = {
      this.cacheRegion = region;
      this.cacheUsage = usage;
      return this;
    }

  }

  final class EntityDefinition(val clazz: Class[_], val entityName: String) {
    var cacheUsage: String = _
    var cacheRegion: String = _

    def this(clazz: Class[_]) {
      this(clazz, Jpas.findEntityName(clazz))
    }

    def cache(region: String, usage: String): this.type = {
      this.cacheRegion = region;
      this.cacheUsage = usage;
      return this;
    }

    override def hashCode: Int = clazz.hashCode()

    override def equals(obj: Any): Boolean = clazz.equals(obj)
  }

  final class CacheConfig(var region: String = null, var usage: String = null) {
  }
}
import EntityPersistConfig._
/**
 * @author chaostone
 * @since 3.1
 */
final class EntityPersistConfig {

  import scala.collection.mutable
  /**
   * Classname -> EntityDefinition
   */
  val entityMap = new mutable.HashMap[String, EntityDefinition]

  /**
   * Classname.property -> CollectionDefinition
   */
  val collectMap = new mutable.HashMap[String, CollectionDefinition]

  val cache = new CacheConfig();

  def entities: Iterable[EntityDefinition] = entityMap.values

  def collections: Iterable[CollectionDefinition] = collectMap.values

  def getEntity(clazz: Class[_]): EntityDefinition = entityMap(clazz.getName)

  def addEntity(definition: EntityDefinition): this.type = {
    entityMap.put(definition.clazz.getName(), definition)
    this
  }

  def addCollection(definition: CollectionDefinition): this.type = {
    collectMap.put(definition.clazz.getName() + definition.property, definition)
    return this
  }
}
