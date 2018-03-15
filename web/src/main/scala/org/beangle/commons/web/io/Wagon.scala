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
package org.beangle.commons.web.io

import java.io.{ File, InputStream }
import java.net.URL
import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.lang.Strings
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.web.util.RequestUtils

/**
 * Stream Downloader
 *
 * @author chaostone
 * @since 2.1
 */
trait Wagon {

  def copy(file: File, req: HttpServletRequest, res: HttpServletResponse): Unit
  def copy(url: URL, req: HttpServletRequest, res: HttpServletResponse): Unit
  def copy(is: InputStream, req: HttpServletRequest, res: HttpServletResponse): Unit
}

object Wagon {

  def setContentHeader(response: HttpServletResponse, attach: String) {
    var contentType = response.getContentType
    if (null == contentType) {
      contentType = MimeTypes.getMimeType(Strings.substringAfterLast(attach, "."), MimeTypes.ApplicationOctetStream).toString
      response.setContentType(contentType)
    }
    RequestUtils.setContentDisposition(response, attach)
  }

  def rename(fileName: String, newName: String): String = {
    var attch_name = ""
    val ext = Strings.substringAfterLast(fileName, ".")
    if (Strings.isBlank(newName)) {
      attch_name = fileName
      var iPos = attch_name.lastIndexOf("\\")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
      iPos = attch_name.lastIndexOf("/")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
    } else {
      attch_name = newName
      if (!attch_name.endsWith("." + ext)) attch_name += "." + ext
    }
    attch_name
  }
}
