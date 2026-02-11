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

import org.beangle.commons.io.{IOs, Resources}
import org.beangle.commons.lang.Strings

import java.net.URL

/** @see https://www.iana.org/assignments/media-types/media-types.xhtml
 * @see  http://www.mime-type.net/
 * @see  https://www.sitepoint.com/mime-types-complete-list/
 */
object MediaTypes {

  val All: MediaType = MediaType("*/*")

  private var registerTypes: Map[String, MediaType] = _

  def atomXml: MediaType = as("application/atom+xml")

  def formUrlencoded: MediaType = as("application/x-www-form-urlencoded")

  def json: MediaType = as("application/json")

  def jsonApi: MediaType = as("application/vnd.api+json")

  def javascript: MediaType = as("application/javascript")

  def stream: MediaType = as("application/octet-stream")

  def xhtmlXml: MediaType = as("application/xhtml+xml")

  def xml: MediaType = as("application/xml")

  def pdf: MediaType = as("application/pdf")

  def ofd: MediaType = as("application/ofd")

  def zip: MediaType = as("application/zip")

  def xlsx: MediaType = as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

  def docx: MediaType = as("application/vnd.openxmlformats-officedocument.wordprocessingml.document")

  def gif: MediaType = as("image/gif")

  def jpeg: MediaType = as("image/jpeg")

  def png: MediaType = as("image/png")

  def multipart: MediaType = as("multipart/form-data")

  def html: MediaType = as("text/html")

  def text: MediaType = as("text/plain")

  def csv: MediaType = as("text/csv")

  def buildTypes(paths: Iterable[URL]): Map[String, MediaType] = {
    val buf = new collection.mutable.HashMap[String, MediaType]
    paths foreach { p =>
      buf ++= readMediaTypes(p)
    }
    buf.put("*/*", All)
    buf.toMap
  }

  private def readMediaTypes(url: URL): Map[String, MediaType] = {
    if null == url then Map.empty
    else
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

  def getTypes: Map[String, MediaType] = {
    if (registerTypes == null) {
      this.registerTypes = buildTypes(Resources.load("org/beangle/commons/activation/mime.types,classpath*:META-INF/mime.types,mime.types"))
    }
    this.registerTypes
  }

  def as(fullName: String): MediaType = {
    getTypes(fullName)
  }

  def get(ext: String, defaultValue: MediaType): MediaType = {
    getTypes.getOrElse(ext, defaultValue)
  }

  def get(ext: String): Option[MediaType] = {
    getTypes.get(ext)
  }

  def parse(str: String): Seq[MediaType] = {
    if null == str then Seq.empty
    else
      val mimeTypes = new collection.mutable.ListBuffer[MediaType]
      Strings.split(str, ",") foreach { token =>
        val commaIndex = token.indexOf(";")
        val mimetype = if (commaIndex > -1) token.substring(0, commaIndex).trim else token.trim
        getTypes.get(mimetype) match {
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
