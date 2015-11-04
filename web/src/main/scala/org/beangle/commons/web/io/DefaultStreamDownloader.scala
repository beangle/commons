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

import org.beangle.commons.activation.{ MimeTypeProvider, MimeTypes }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * Default Stream Downloader
 *
 * @author chaostone
 * @since 2.4
 */
class DefaultStreamDownloader extends StreamDownloader with Logging {

  override def download(req: HttpServletRequest, res: HttpServletResponse, url: URL, fileName: String) {
    try {
      download(req, res, url.openStream(), fileName)
    } catch {
      case e: Exception => logger.error(s"download file error=$fileName", e)
    }
  }

  override def download(req: HttpServletRequest, res: HttpServletResponse, file: File, fileName: String) {
    if (file.exists()) {
      try {
        download(req, res, new FileInputStream(file), fileName)
      } catch {
        case e: Exception => logger.error(s"download file error=$fileName", e)
      }
    }
  }

  override def download(req: HttpServletRequest, res: HttpServletResponse, is: InputStream, fileName: String) {
    try {
      res.reset()
      setContentHeader(res, fileName)
      IOs.copy(is, res.getOutputStream)
    } catch {
      case e: Exception => logger.error(s"download file error $fileName", e)
    } finally {
      IOs.close(is)
    }
  }

  protected def setContentHeader(response: HttpServletResponse, attach: String) {
    var contentType = response.getContentType
    if (null == contentType) {
      contentType = MimeTypeProvider.getMimeType(Strings.substringAfterLast(attach, "."), MimeTypes.ApplicationOctetStream).toString
      response.setContentType(contentType)
    }
    RequestUtils.setContentDisposition(response, attach)
  }

}
