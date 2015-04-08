/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.web.io

import java.io.{ File, FileInputStream, InputStream }
import java.net.URL

import scala.annotation.elidable
import scala.annotation.elidable.FINE

import org.beangle.commons.activation.{ MimeTypeProvider, MimeTypes }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * Default Stream Downloader
 *
 * @author chaostone
 * @since 2.4
 */
class DefaultStreamDownloader extends StreamDownloader {

  def download(request: HttpServletRequest, response: HttpServletResponse, file: File) {
    download(request, response, file, file.getName)
  }

  def download(request: HttpServletRequest, response: HttpServletResponse, url: URL, display: String) {
    try {
      download(request, response, url.openStream(), url.getFile, display)
    } catch {
      case e: Exception => request.getServletContext.log(s"download file error=$display", e)
    }
  }

  def download(request: HttpServletRequest, response: HttpServletResponse, file: File, display: String) {
    if (file.exists()) {
      try {
        download(request, response, new FileInputStream(file), file.getAbsolutePath, display)
      } catch {
        case e: Exception => request.getServletContext.log(s"download file error=$display", e)
      }
    }
  }

  def download(request: HttpServletRequest, response: HttpServletResponse, inStream: InputStream, name: String, display: String) {
    val attach_name = getAttachName(name, display)
    try {
      response.reset()
      addContent(request, response, attach_name)
      IOs.copy(inStream, response.getOutputStream)
    } catch {
      case e: Exception => System.err.println(s"download file error $attach_name", e)
    } finally {
      IOs.close(inStream)
    }
  }

  protected def getAttachName(name: String, display: String): String = {
    var attch_name = ""
    val ext = Strings.substringAfterLast(name, ".")
    if (Strings.isBlank(display)) {
      attch_name = name
      var iPos = attch_name.lastIndexOf("\\")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
      iPos = attch_name.lastIndexOf("/")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
    } else {
      attch_name = display
      if (!attch_name.endsWith("." + ext)) attch_name += "." + ext
    }
    attch_name
  }

  protected def addContent(request: HttpServletRequest, response: HttpServletResponse, attach: String) {
    var contentType = response.getContentType
    if (null == contentType) {
      contentType = MimeTypeProvider.getMimeType(Strings.substringAfterLast(attach, "."), MimeTypes.ApplicationOctetStream).toString
      response.setContentType(contentType)
    }
    RequestUtils.setFileDownloadHeader(response, attach)
  }

}
