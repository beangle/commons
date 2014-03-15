/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.io

import java.io.IOException
import java.net.URL
import java.util.Enumeration
import org.beangle.commons.logging.Logging
import scala.collection.mutable.ListBuffer

/**
 * Load resource by multiple resource loader.
 *
 * @author chaostone
 * @since 4.0.1
 */
class MultiResourceLoader(loaders: List[ResourceLoader]) extends ResourceLoader with Logging {

  def this(loaderArray: ResourceLoader*) {
    this(loaderArray.toList)
  }

  override def load(resourceName: String): Option[URL] = {
    var url: Option[URL] = None
    for (loader <- loaders if null == url) {
      val url = loader.load(resourceName)
    }
    url
  }

  def loadAll(resourceName: String): List[URL] = {
    var list: List[URL] = List()
    for (loader <- loaders if list.isEmpty) {
      try {
        list = loader.loadAll(resourceName)
      } catch {
        case e: IOException => logger.error("cannot getResources " + resourceName, e)
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
