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

/** MIME types registry.
 *
 * @see https://www.iana.org/assignments/media-types/media-types.xhtml
 * @see http://www.mime-type.net/
 * @see https://www.sitepoint.com/mime-types-complete-list/
 */
object MediaTypes {

  /** Wildcard media type (&#42;/&#42;). */
  val All: MediaType = MediaType("*/*")

  private var registerTypes: Map[String, MediaType] = _

  /** application/atom+xml */
  def atomXml: MediaType = as("application/atom+xml")

  /** application/x-www-form-urlencoded */
  def formUrlencoded: MediaType = as("application/x-www-form-urlencoded")

  /** application/json */
  def json: MediaType = as("application/json")

  /** application/vnd.api+json */
  def jsonApi: MediaType = as("application/vnd.api+json")

  /** application/javascript */
  def javascript: MediaType = as("application/javascript")

  /** application/octet-stream */
  def stream: MediaType = as("application/octet-stream")

  /** application/xhtml+xml */
  def xhtmlXml: MediaType = as("application/xhtml+xml")

  /** application/xml */
  def xml: MediaType = as("application/xml")

  /** application/pdf */
  def pdf: MediaType = as("application/pdf")

  /** application/ofd */
  def ofd: MediaType = as("application/ofd")

  /** application/zip */
  def zip: MediaType = as("application/zip")

  /** application/vnd.openxmlformats-officedocument.spreadsheetml.sheet */
  def xlsx: MediaType = as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

  /** application/vnd.openxmlformats-officedocument.wordprocessingml.document */
  def docx: MediaType = as("application/vnd.openxmlformats-officedocument.wordprocessingml.document")

  /** image/gif */
  def gif: MediaType = as("image/gif")

  /** image/jpeg */
  def jpeg: MediaType = as("image/jpeg")

  /** image/png */
  def png: MediaType = as("image/png")

  /** multipart/form-data */
  def multipart: MediaType = as("multipart/form-data")

  /** text/html */
  def html: MediaType = as("text/html")

  /** text/plain */
  def text: MediaType = as("text/plain")

  /** text/csv */
  def csv: MediaType = as("text/csv")

  /** Builds type map from config URLs (extension or full name -> MediaType).
   *
   * @param paths URLs to mime.types config files
   * @return map of extension/fullName to MediaType
   */
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

  /** Returns all registered media types. */
  def getTypes: Map[String, MediaType] = {
    if (registerTypes == null) {
      this.registerTypes = buildTypes(Resources.load("org/beangle/commons/activation/mime.types,classpath*:META-INF/mime.types,mime.types"))
    }
    this.registerTypes
  }

  /** Returns MediaType by full name (e.g. "text/plain").
   *
   * @param fullName MIME type string
   * @return MediaType
   */
  def as(fullName: String): MediaType = {
    getTypes(fullName)
  }

  /** Returns MediaType by extension or full name, or default if not found.
   *
   * @param ext          file extension or MIME full name
   * @param defaultValue fallback when not found
   * @return MediaType
   */
  def get(ext: String, defaultValue: MediaType): MediaType = {
    getTypes.getOrElse(ext, defaultValue)
  }

  /** Returns MediaType by extension or full name.
   *
   * @param ext file extension or MIME full name
   * @return Some(MediaType) or None
   */
  def get(ext: String): Option[MediaType] = {
    getTypes.get(ext)
  }

  /** Parses Accept-style header string to MediaType sequence.
   *
   * @param str Accept header value (comma-separated)
   * @return sequence of MediaType
   */
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

/** MediaType companion. */
object MediaType {
  /** Creates MediaType from token (e.g. "text/plain" or "text/plain;charset=utf-8").
   *
   * @param token MIME type string, optionally with params
   * @return MediaType
   */
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

/** MIME type (primary/subtype, e.g. text/plain). */
class MediaType(val primaryType: String, val subType: String) {
  def this(pt: String) = {
    this(pt, "*")
  }

  override def toString: String =
    if (subType == "*") primaryType else primaryType + "/" + subType
}
