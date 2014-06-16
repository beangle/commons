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
package org.beangle.commons.http.mime

import java.io.{ IOException, InputStream }
import java.net.URL
import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.io.IOs

class MimeTypeProvider extends Logging {

  private var contentTypes: Map[String, String] = Map.empty

  private var resources: Resources = _

  def getMimeType(ext: String, defaultValue: String): String = contentTypes.get(ext).getOrElse(defaultValue)

  def getMimeType(ext: String): Option[String] = contentTypes.get(ext)

  def getResources() = resources

  def setResources(resources: Resources) {
    this.resources = resources
    val buf = new collection.mutable.HashMap[String, String]
    if (null != resources) {
      buf ++= IOs.readJavaProperties(resources.global)
      if (null != resources.locals) {
        resources.locals foreach { path => buf ++= IOs.readJavaProperties(path) }
      }
      buf ++= IOs.readJavaProperties(resources.user)
    }
    contentTypes = buf.toMap
  }
}
