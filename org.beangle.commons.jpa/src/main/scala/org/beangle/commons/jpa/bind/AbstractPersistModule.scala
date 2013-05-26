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

import java.util.List

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.Entity
import scala.collection.JavaConversions._
import org.beangle.commons.jpa.bind.EntityPersistConfig._

abstract class AbstractPersistModule {

  private var config :EntityPersistConfig = _

  protected def doConfig()

  protected final def add(classes:Class[_ <: Entity[_]]*): EntityHolder= {
    for (cls <- classes)
      config.addEntity(new EntityDefinition(cls))
    new EntityHolder(config,classes: _*)
  }

  protected final def cache(region:String):CacheHolder = {
    new CacheHolder(config).cache(region).usage(config.cache.usage);
  }

  protected final def cache():CacheHolder= {
    new CacheHolder(config).cache(config.cache.region).usage(config.cache.usage);
  }

  protected final def collection(clazz:Class[_],properties:String*):List[CollectionDefinition] = {
    val definitions = CollectUtils.newArrayList[CollectionDefinition](properties.length);
    for (property <- properties) {
      definitions.add(new CollectionDefinition(clazz, property));
    }
    definitions
  }

  protected final def defaultCache(region:String,usage: String) {
    config.cache.region = region;
    config.cache.usage =usage;
  }

  final def getConfig(): EntityPersistConfig= {
    config = new EntityPersistConfig()
    doConfig()
    config
  }

  final class CacheHolder(val config:EntityPersistConfig) {
    var cacheUsage: String =_
    var cacheRegion:  String =_

    def add(first:List[CollectionDefinition],definitionLists:List[CollectionDefinition]*):this.type ={
      for (definition <- first) {
        config.addCollection(definition.cache(cacheRegion, cacheUsage));
      }
      for (definitions <- definitionLists) {
        for (definition <- definitions) {
          config.addCollection(definition.cache(cacheRegion, cacheUsage));
        }
      }
      this
    }

    def add(first:Class[_ <: Entity[_]],classes:Class[_ <: Entity[_]]*):this.type= {
        config.getEntity(first).cache(cacheRegion, cacheUsage)
      for (clazz <- classes)
        config.getEntity(clazz).cache(cacheRegion, cacheUsage)
      this
    }

    def usage(cacheUsage:String):this.type = {
      this.cacheUsage = cacheUsage
      this
    }

    def cache(cacheRegion:String):this.type= {
      this.cacheRegion = cacheRegion
      this
    }

  }

  final class EntityHolder(val config:EntityPersistConfig,val classes:Class[_]*) {

    def cacheable() :EntityHolder ={
      for (clazz <- classes) {
        config.getEntity(clazz).cache(config.cache.region, config.cache.usage);
      }
      this
    }

    def cache(region:String):EntityHolder ={
      for (clazz <- classes) {
        config.getEntity(clazz).cacheRegion = region
      }
      return this
    }

    def usage(usage:String): EntityHolder= {
      for (clazz <- classes) {
        config.getEntity(clazz).cacheUsage = usage
      }
      this
    }

  }
}
