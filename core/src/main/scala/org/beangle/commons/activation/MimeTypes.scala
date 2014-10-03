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
package org.beangle.commons.activation

import org.beangle.commons.inject.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders.{ getResource, getResources }
import org.beangle.commons.logging.Logging
import javax.activation.MimeType
import java.net.URL
import org.beangle.commons.lang.Strings

object MimeTypes {

  val All = this("*/*")

  val ApplicationAtomXml = this("application/atom+xml")

  val ApplicationFormUrlencoded = this("application/x-www-form-urlencoded");

  val ApplicationJson = this("application/json")

  val ApplicationOctetStream = this("application/octet-stream");

  val ApplicationXhtmlXml = this("application/xhtml+xml")

  val ApplicationXml = this("application/xml")

  val ImageGif = this("image/gif");

  val ImageJpeg = this("image/jpeg")

  val ImagePng = this("image/png")

  val MultipartFormData = this("multipart/form-data")

  val TextHtml = this("text/html")

  val TextPlain = this("text/plain")

  val TextXml = this("text/xml")

  def apply(mimeType: String): javax.activation.MimeType = {
    new javax.activation.MimeType(mimeType)
  }

  def buildMimeTypes(resources: Resources): Map[String, MimeType] = {
    val buf = new collection.mutable.HashMap[String, MimeType]
    if (null != resources) {
      buf ++= readMimeTypes(resources.global)
      if (null != resources.locals) resources.locals foreach { path => buf ++= readMimeTypes(path) }
      buf ++= parse(IOs.readJavaProperties(resources.user))
    }
    buf.toMap
  }

  private def readMimeTypes(url: URL): Map[String, MimeType] = {
    if (null == url) Map.empty
    val buf = new collection.mutable.HashMap[String, MimeType]
    IOs.readLines(url.openStream()) foreach { line =>
      if (Strings.isNotBlank(line) && !line.startsWith("#")) {
        val mimetype = new MimeType(Strings.substringBetween(line, "=", "exts").trim)
        Strings.split(Strings.substringAfter(line, "exts").trim.substring(1), ',') foreach { ext =>
          buf.put(ext.trim, mimetype)
        }
      }
    }
    buf.toMap
  }

  private def parse(mimetypes: Map[String, String]): Map[String, MimeType] = {
    mimetypes.map {
      case (ext, mimetype) =>
        (ext, MimeTypes(mimetype))
    }
  }

  def mimeTypeResources: Resources = {
    new Resources(getResource("org/beangle/commons/activation/mime.types"),
      getResources("META-INF/mime.types"), getResource("mime.types"))
  }
}

object MimeTypeProvider extends Logging {

  import MimeTypes._
  private val contentTypes: Map[String, MimeType] = buildMimeTypes(mimeTypeResources)

  def getMimeType(ext: String, defaultValue: MimeType): MimeType = contentTypes.get(ext).getOrElse(defaultValue)

  def getMimeType(ext: String): Option[MimeType] = contentTypes.get(ext)

}
