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
package org.beangle.commons.http.mime

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Properties
import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.time.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import MimeTypeProvider._
import scala.beans.{ BeanProperty, BooleanBeanProperty }
//remove if not needed
import scala.collection.JavaConversions._

object MimeTypeProvider {

  private var logger: Logger = LoggerFactory.getLogger(classOf[MimeTypeProvider])
}

class MimeTypeProvider {

  private val contentTypes = new Properties()

  var resources: Resources = _

  def getMimeType(ext: String, defaultValue: String): String = {
    contentTypes.getProperty(ext, defaultValue)
  }

  def getMimeType(ext: String): String = contentTypes.getProperty(ext)

  /**
   * META-INF/mimetypes.properties
   *
   * @param url
   */
  private def loadMimeType(url: URL) {
    try {
      val watch = new Stopwatch(true)
      val im = url.openStream()
      contentTypes.load(im)
      logger.info("Load {} content types in {}", contentTypes.size, watch)
      im.close()
    } catch {
      case e: IOException => logger.error("load " + url + " error", e)
    }
  }

  def getResources() = resources

  def setResources(resources: Resources) {
    this.resources = resources
    if (null == resources) return
    if (null != resources.getGlobal) {
      loadMimeType(resources.getGlobal)
    }
    if (null != resources.getLocals) {
      for (path <- resources.getLocals) {
        loadMimeType(path)
      }
    }
    if (null != resources.getUser) {
      loadMimeType(resources.getUser)
    }
  }
}
