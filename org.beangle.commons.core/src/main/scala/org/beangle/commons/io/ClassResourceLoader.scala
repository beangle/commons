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
package org.beangle.commons.io

import java.io.IOException
import java.net.URL
import java.util.Enumeration
import org.beangle.commons.collection.CollectUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ClassResourceLoader._
import scala.collection.mutable.ListBuffer

object ClassResourceLoader {

  private val logger = LoggerFactory.getLogger(classOf[ClassResourceLoader])
}

/**
 * Load resource by class loader.
 *
 * @author chaostone
 * @since 3.3.0
 */
class ClassResourceLoader(loaders: List[ClassLoader]) extends ResourceLoader {

  def this(loaderArray: ClassLoader*) {
    this(loaderArray.toList)
  }

  override def getResource(resourceName: String): Option[URL] = {
    for (loader <- loaders) {
      val url = loader.getResource(resourceName)
      if (null != url) return Some(url)
    }
    None
  }

  def getResources(resourceName: String): List[URL] = {
    var em: Enumeration[URL] = null
    for (loader <- loaders if (null != em && em.hasMoreElements)) {
      try {
        em = loader.getResources(resourceName)
      } catch {
        case e: IOException => logger.error("cannot getResources " + resourceName, e)
      }
    }
    val urls = new ListBuffer[URL]
    while (null != em && em.hasMoreElements()) urls += em.nextElement()
    urls.toList
  }
}
