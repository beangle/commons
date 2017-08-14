/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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

import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders.{ getResource, getResources }
import org.beangle.commons.lang.Strings

import javax.activation.MimeType

/**
 * @see https://www.iana.org/assignments/media-types/media-types.xhtml
 * @see http://www.mime-type.net/
 * @see https://www.sitepoint.com/mime-types-complete-list/
 */
object MimeTypes {

  val All = this("*/*")

  val ApplicationAtomXml = this("application/atom+xml")

  val ApplicationFormUrlencoded = this("application/x-www-form-urlencoded");

  val ApplicationJson = this("application/json")

  val ApplicationJsonApi = this("application/vnd.api+json")

  val ApplicationJavascript = this("application/javascript")

  val ApplicationOctetStream = this("application/octet-stream");

  val ApplicationXhtmlXml = this("application/xhtml+xml")

  val ApplicationXml = this("application/xml")

  val ApplicationPdf = this("application/pdf")

  val ImageGif = this("image/gif");

  val ImageJpeg = this("image/jpeg")

  val ImagePng = this("image/png")

  val MultipartFormData = this("multipart/form-data")

  val TextHtml = this("text/html")

  val TextPlain = this("text/plain")

  val TextXml = this("text/xml")

  val TextCsv = this("text/csv")

  val TextJavaScript = this("text/javascript")

  def apply(mimeType: String): javax.activation.MimeType = {
    new javax.activation.MimeType(mimeType)
  }

  def buildMimeTypes(resources: Resources): Map[String, MimeType] = {
    val buf = new collection.mutable.HashMap[String, MimeType]
    if (null != resources) {
      resources.paths foreach { p =>
        buf ++= readMimeTypes(p)
      }
    }
    buf.toMap
  }

  private def readMimeTypes(url: URL): Map[String, MimeType] = {
    if (null == url) return Map.empty
    val buf = new collection.mutable.HashMap[String, MimeType]
    val types = new collection.mutable.HashSet[String]
    IOs.readLines(url.openStream()) foreach { line =>
      if (Strings.isNotBlank(line) && !line.startsWith("#")) {
        val mimetypeStr = Strings.substringBetween(line, "=", "exts").trim
        assert(!types.contains(mimetypeStr), "duplicate mime type:" + mimetypeStr)
        types += mimetypeStr

        val mimetype = new MimeType(mimetypeStr)
        Strings.split(Strings.substringAfter(line, "exts").trim.substring(1), ',') foreach { ext =>
          val extension = ext.trim
          val exists = buf.get(extension)
          assert(exists.isEmpty, s"exists $extension = " + exists.get + ", the newer is " + mimetype)
          buf.put(extension, mimetype)
        }
      }
    }
    buf.toMap
  }

  def parse(str: String): Seq[MimeType] = {
    if (null == str) return Seq.empty

    val mimeTypes = new collection.mutable.ListBuffer[MimeType]
    str.split(",\\s*") foreach { token =>
      val commaIndex = token.indexOf(";")
      val mimetype = if (commaIndex > -1) token.substring(0, commaIndex).trim else token.trim
      mimeTypes += new MimeType(mimetype)
    }
    mimeTypes.toList
  }

  def mimeTypeResources: Resources = {
    new Resources(getResource("org/beangle/commons/activation/mime.types"),
      getResources("META-INF/mime.types"), getResource("mime.types"))
  }
}

object MimeTypeProvider {

  import MimeTypes._
  private val contentTypes: Map[String, MimeType] = buildMimeTypes(mimeTypeResources)

  def getMimeType(ext: String, defaultValue: MimeType): MimeType = contentTypes.get(ext).getOrElse(defaultValue)

  def getMimeType(ext: String): Option[MimeType] = contentTypes.get(ext)

}
