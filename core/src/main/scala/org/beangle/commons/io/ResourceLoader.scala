/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.io

import java.io.IOException
import java.net.URL

import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.logging.Logging

/**
  * Resource loader
  *
  * @author chaostone
  * @since 3.3.0
  */
trait ResourceLoader {

  def load(resourceName: String): Option[URL]

  def loadAll(resourceName: String): List[URL]

  def load(names: Seq[String]): List[URL]

}

class MultiResourceLoader(loaders: List[ResourceLoader]) extends ResourceLoader with Logging {

  def this(loaderArray: ResourceLoader*) {
    this(loaderArray.toList)
  }

  override def load(resourceName: String): Option[URL] = {
    var url: Option[URL] = None
    for (loader <- loaders if url.isEmpty) {
      url = loader.load(resourceName)
    }
    url
  }

  def loadAll(resourceName: String): List[URL] = {
    var list: List[URL] = List()
    for (loader <- loaders if list.isEmpty) {
      try {
        list = loader.loadAll(resourceName)
      } catch {
        case e: IOException =>
          logger.error("cannot getResources " + resourceName, e)
      }
    }
    list
  }

  def load(names: Seq[String]): List[URL] = {
    val urls = new collection.mutable.ListBuffer[URL]
    for (name <- names)
      urls ++= load(name)
    urls.toList
  }
}

/**
  * Load resource by class loader.
  */
class ClasspathResourceLoader(val prefixes: List[String] = List("")) extends ResourceLoader {

  def this(prefixStr: String) {
    this(if (Strings.isEmpty(prefixStr)) List("") else Strings.split(prefixStr, " ").toList)
  }

  def loadAll(resourceName: String): List[URL] = {
    val urls = new collection.mutable.ListBuffer[URL]
    for (prefix <- prefixes)
      urls ++= ClassLoaders.getResources(prefix + resourceName)
    urls.toList
  }

  def load(name: String): Option[URL] = {
    var url: Option[URL] = None
    for (prefix <- prefixes; if url.isEmpty) {
      url = ClassLoaders.getResource(prefix + name)
    }
    url
  }

  def load(names: Seq[String]): List[URL] = {
    val urls = new collection.mutable.ListBuffer[URL]
    for (name <- names) {
      val url = load(name)
      if (url.isDefined) urls += url.get
    }
    urls.toList
  }

}
