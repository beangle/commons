/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.activation

import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders.{ getResource, getResources }
import org.beangle.commons.lang.Strings

/**
 * @see https://www.iana.org/assignments/media-types/media-types.xhtml
 * @see http://www.mime-type.net/
 * @see https://www.sitepoint.com/mime-types-complete-list/
 */
object MediaTypes {

  val All = MediaType("*/*")

  private val types: Map[String, MediaType] =
    buildTypes(new Resources(
      getResource("org/beangle/commons/activation/mime.types"),
      getResources("META-INF/mime.types"), getResource("mime.types")))

  val ApplicationAtomXml: MediaType = types("application/atom+xml")

  val ApplicationFormUrlencoded: MediaType = types("application/x-www-form-urlencoded")

  val ApplicationJson: MediaType = types("application/json")

  val ApplicationJsonApi: MediaType = types("application/vnd.api+json")

  val ApplicationJavascript: MediaType = types("application/javascript")

  val ApplicationOctetStream: MediaType = types("application/octet-stream")

  val ApplicationXhtmlXml: MediaType = types("application/xhtml+xml")

  val ApplicationXml: MediaType = types("application/xml")

  val ApplicationPdf: MediaType = types("application/pdf")

  val ApplicationXlsx: MediaType = types("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

  val ApplicationDocx: MediaType = types("application/vnd.openxmlformats-officedocument.wordprocessingml.document")

  val ImageGif: MediaType = types("image/gif")

  val ImageJpeg: MediaType = types("image/jpeg")

  val ImagePng: MediaType = types("image/png")

  val MultipartFormData: MediaType = types("multipart/form-data")

  val TextHtml: MediaType = types("text/html")

  val TextPlain: MediaType = types("text/plain")

  val TextCsv: MediaType = types("text/csv")

  def buildTypes(resources: Resources): Map[String, MediaType] = {
    val buf = new collection.mutable.HashMap[String, MediaType]
    if (null != resources)
      resources.paths foreach { p =>
        buf ++= readMediaTypes(p)
      }
    buf.put("*/*", All)
    buf.toMap
  }

  private def readMediaTypes(url: URL): Map[String, MediaType] =
    if (null == url)
      Map.empty
    else {
      val buf = new collection.mutable.HashMap[String, MediaType]
      IOs.readLines(url.openStream()) foreach { line =>
        if (Strings.isNotBlank(line) && !line.startsWith("#")) {
          val mimetypeStr = Strings.substringBetween(line, "=", "exts").trim
          require(!buf.contains(mimetypeStr), "duplicate mime type:" + mimetypeStr)
          val mimetype = MediaType(mimetypeStr)
          buf.put(mimetypeStr, mimetype)

          val exts = Strings.substringAfter(line, "exts").trim.substring(1)
          if (Strings.isNotBlank(exts))
            Strings.split(exts, ',') foreach { ext =>
              val extension = ext.trim
              val exists = buf.get(extension)
              require(exists.isEmpty, s"exists $extension = " + exists.get + ", the newer is " + mimetype)
              buf.put(extension, mimetype)
            }
        }
      }
      buf.toMap
    }

  def get(ext: String, defaultValue: MediaType): MediaType =
    types.getOrElse(ext, defaultValue)

  def get(ext: String): Option[MediaType] =
    types.get(ext)

  def parse(str: String): Seq[MediaType] =
    if (null == str)
      Seq.empty
    else {
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
    if (-1 == slashIndex)
      new MediaType(mimetype)
    else
      new MediaType(mimetype.substring(0, slashIndex), mimetype.substring(slashIndex + 1))
  }
}

class MediaType(val primaryType: String, val subType: String) {
  def this(pt: String) = {
    this(pt, "*")
  }

  override def toString: String =
    if (subType == "*") primaryType else primaryType + "/" + subType
}
