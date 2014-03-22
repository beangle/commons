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
package org.beangle.commons.web.io

import org.beangle.commons.web.util.RequestUtils.encodeAttachName
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.bean.Initializing
import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.IOs

/**
 * Default Stream Downloader
 *
 * @author chaostone
 * @since 2.4
 */
class DefaultStreamDownloader(protected var mimeTypeProvider: MimeTypeProvider) extends Initializing with StreamDownloader with Logging {

  def init() {
    Assert.notNull(mimeTypeProvider, "mimeTypeProvider must be set");
  }

  def download(request: HttpServletRequest, response: HttpServletResponse, file: File) {
    download(request, response, file, file.getName)
  }

  def download(request: HttpServletRequest,
    response: HttpServletResponse,
    url: URL,
    display: String) {
    try {
      download(request, response, url.openStream(), url.getFile, display)
    } catch {
      case e: Exception => logger.warn("download file error=" + display, e)
    }
  }

  def download(request: HttpServletRequest,
    response: HttpServletResponse,
    file: File,
    display: String) {
    file.exists()
    try {
      download(request, response, new FileInputStream(file), file.getAbsolutePath, display)
    } catch {
      case e: Exception => logger.warn("download file error=" + display, e)
    }
  }

  protected def addContent(request: HttpServletRequest, response: HttpServletResponse, attach: String) {
    var contentType = response.getContentType
    if (null == contentType) {
      contentType = mimeTypeProvider.getMimeType(Strings.substringAfterLast(attach, "."), "application/x-msdownload")
      response.setContentType(contentType)
      logger.debug("set content type {} for {}", contentType, attach)
    }
    val encodeName = encodeAttachName(request, attach)
    response.setHeader("Content-Disposition", "attachment; filename=" + encodeName)
    response.setHeader("Location", encodeName)
  }

  def download(request: HttpServletRequest,
    response: HttpServletResponse,
    inStream: InputStream,
    name: String,
    display: String) {
    val attach_name = getAttachName(name, display)
    try {
      response.reset()
      addContent(request, response, attach_name)
      IOs.copy(inStream, response.getOutputStream)
    } catch {
      case e: Exception => logger.warn("download file error " + attach_name, e)
    } finally {
      IOs.close(inStream)
    }
  }

  def setMimeTypeProvider(mimeTypeProvider: MimeTypeProvider) {
    this.mimeTypeProvider = mimeTypeProvider
  }

  def getAttachName(name: String, display: String): String = {
    var attch_name = ""
    val ext = Strings.substringAfterLast(name, ".")
    if (Strings.isBlank(display)) {
      attch_name = getFileName(name)
    } else {
      attch_name = display
      if (!attch_name.endsWith("." + ext)) {
        attch_name += "." + ext
      }
    }
    attch_name
  }

  /**
   * Returns the file name by path.
   *
   * @param file_name
   */
  protected def getFileName(fileName: String): String = {
    if (fileName == null) return ""
    var file_name = fileName.trim()
    var iPos = 0
    iPos = file_name.lastIndexOf("\\")
    if (iPos > -1) file_name = file_name.substring(iPos + 1)
    iPos = file_name.lastIndexOf("/")
    if (iPos > -1) file_name = file_name.substring(iPos + 1)
    iPos = file_name.lastIndexOf(File.separator)
    if (iPos > -1) file_name = file_name.substring(iPos + 1)
    file_name
  }
}
