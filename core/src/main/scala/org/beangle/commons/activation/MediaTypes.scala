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
package org.beangle.commons.activation

import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders.{getResource, getResources}
import org.beangle.commons.lang.Strings

/**
  * @see https://www.iana.org/assignments/media-types/media-types.xhtml
  * @see http://www.mime-type.net/
  * @see https://www.sitepoint.com/mime-types-complete-list/
  */
object MediaTypes {

  val All = MediaType("*/*")

  private val types: Map[String, MediaType] = {
    buildTypes(new Resources(getResource("org/beangle/commons/activation/mime.types"),
      getResources("META-INF/mime.types"), getResource("mime.types")))
  }

  val ApplicationAtomXml = types("application/atom+xml")

  val ApplicationFormUrlencoded = types("application/x-www-form-urlencoded")

  val ApplicationJson = types("application/json")

  val ApplicationJsonApi = types("application/vnd.api+json")

  val ApplicationJavascript = types("application/javascript")

  val ApplicationOctetStream = types("application/octet-stream")

  val ApplicationXhtmlXml = types("application/xhtml+xml")

  val ApplicationXml = types("application/xml")

  val ApplicationPdf = types("application/pdf")

  val ImageGif = types("image/gif");

  val ImageJpeg = types("image/jpeg")

  val ImagePng = types("image/png")

  val MultipartFormData = types("multipart/form-data")

  val TextHtml = types("text/html")

  val TextPlain = types("text/plain")

  val TextCsv = types("text/csv")

  def buildTypes(resources: Resources): Map[String, MediaType] = {
    val buf = new collection.mutable.HashMap[String, MediaType]
    if (null != resources) {
      resources.paths foreach { p =>
        buf ++= readMediaTypes(p)
      }
    }
    buf.put("*/*", All)
    buf.toMap
  }

  private def readMediaTypes(url: URL): Map[String, MediaType] = {
    if (null == url) return Map.empty
    val buf = new collection.mutable.HashMap[String, MediaType]
    IOs.readLines(url.openStream()) foreach { line =>
      if (Strings.isNotBlank(line) && !line.startsWith("#")) {
        val mimetypeStr = Strings.substringBetween(line, "=", "exts").trim
        assert(!buf.contains(mimetypeStr), "duplicate mime type:" + mimetypeStr)
        val mimetype = MediaType(mimetypeStr)
        buf.put(mimetypeStr, mimetype)

        val exts = Strings.substringAfter(line, "exts").trim.substring(1)
        if (Strings.isNotBlank(exts)) {
          Strings.split(exts, ',') foreach { ext =>
            val extension = ext.trim
            val exists = buf.get(extension)
            assert(exists.isEmpty, s"exists $extension = " + exists.get + ", the newer is " + mimetype)
            buf.put(extension, mimetype)
          }
        }
      }
    }
    buf.toMap
  }

  def get(ext: String, defaultValue: MediaType): MediaType = {
    types.get(ext).getOrElse(defaultValue)
  }

  def get(ext: String): Option[MediaType] = {
    types.get(ext)
  }

  def parse(str: String): Seq[MediaType] = {
    if (null == str) return Seq.empty

    val mimeTypes = new collection.mutable.ListBuffer[MediaType]
    Strings.split(str, ",") foreach { token =>
      val commaIndex = token.indexOf(";")
      val mimetype = if (commaIndex > -1) token.substring(0, commaIndex).trim else token.trim
      types.get(mimetype) match {
        case Some(mt) => mimeTypes += mt
        case None => MediaType(mimetype)
      }
    }
    mimeTypes.toList
  }
}

object MediaType {
  def apply(token: String): MediaType = {
      val commaIndex = token.indexOf(";")
      val mimetype = if (commaIndex > -1) token.substring(0, commaIndex).trim else token.trim
      val slashIndex = token.indexOf("/")
      if (-1 == slashIndex) {
        new MediaType(mimetype)
      }
      else {
        new MediaType(mimetype.substring(0, slashIndex), mimetype.substring(slashIndex + 1))
      }
  }
}

class MediaType(val primaryType: String, val subType: String) {
  def this(pt: String) {
    this(pt, "*")
  }

  override def toString: String = {
    if (subType == "*") primaryType else primaryType + "/" + subType
  }
}