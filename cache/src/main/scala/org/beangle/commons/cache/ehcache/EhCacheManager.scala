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
package org.beangle.commons.cache.ehcache

import java.net.URL
import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import org.beangle.commons.cache.AbstractCacheManager
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.Cache
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.logging.Logging
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.{ CacheConfigurationBuilder, CacheManagerBuilder, ConfigurationBuilder }
import org.ehcache.xml.XmlConfiguration
import org.ehcache.core.config.DefaultConfiguration
import org.ehcache.spi.service.ServiceCreationConfiguration

/**
 * @author chaostone
 */
class EhCacheManager(val name: String = "ehcache", autoCreate: Boolean = false) extends AbstractCacheManager(autoCreate)
    with Initializing with Logging {

  var configUrl: URL = _

  private var xmlConfig: XmlConfiguration = _

  private var underlingManager: org.ehcache.CacheManager = _

  def init(): Unit = {
    assert(null != name)
    if (null == configUrl) {
      configUrl = ClassLoaders.getResource(name + ".xml")
      if (null == configUrl) logger.warn(s"Cannot find ${name}.xml in classpath.")
    }
    underlingManager =
      if (null != configUrl) {
        xmlConfig = new XmlConfiguration(configUrl)
        CacheManagerBuilder.newCacheManager(xmlConfig)
      } else {
        val config = new DefaultConfiguration(null.asInstanceOf[ClassLoader], Array.empty[ServiceCreationConfiguration[_]]: _*)
        CacheManagerBuilder.newCacheManager(config)
      }
    underlingManager.init()
  }

  protected override def newCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    val c = underlingManager.getCache(name, keyType, valueType)
    if (null == c) {
      val builder = getConfigBuilder(name + ".Template", keyType, valueType)
      new EhCache(underlingManager.createCache(name, builder.build()))
    } else {
      new EhCache(c)
    }
  }

  protected[ehcache] def newCache[K, V](name: String, config: CacheConfiguration[K, V]): Cache[K, V] = {
    val newer = new EhCache(underlingManager.createCache(name, config))
    register(name, newer)
    newer
  }

  protected override def findCache[K, V](name: String, keyType: Class[K], valueType: Class[V]): Cache[K, V] = {
    val ul = underlingManager.getCache(name, keyType, valueType)
    if (null == ul) null else new EhCache(ul)
  }

  protected[ehcache] def getEhcache[K, V](name: String, keyType: Class[K], valueType: Class[V]): org.ehcache.Cache[K, V] = {
    underlingManager.getCache(name, keyType, valueType)
  }

  protected[ehcache] def getConfigBuilder[K, V](template: String, keyType: Class[K], valueType: Class[V]): CacheConfigurationBuilder[K, V] = {
    var builder: CacheConfigurationBuilder[K, V] = null
    if (null != xmlConfig) builder = xmlConfig.newCacheConfigurationBuilderFromTemplate(template, keyType, valueType)
    //if (null == builder) builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType, null)
    if (null == builder) {
      throw new RuntimeException("Cannot get a cache config builder for " + template)
    }
    builder

  }

  override def destroy(): Unit = {
    underlingManager.close()
  }
}
