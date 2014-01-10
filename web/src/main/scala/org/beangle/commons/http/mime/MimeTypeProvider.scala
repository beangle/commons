/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.http.mime

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Properties
import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging

class MimeTypeProvider extends Logging {

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
    if (null != resources.global) {
      loadMimeType(resources.global)
    }
    if (null != resources.locals) {
      for (path <- resources.locals) {
        loadMimeType(path)
      }
    }
    if (null != resources.user) loadMimeType(resources.user)
  }
}
