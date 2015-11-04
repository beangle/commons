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

import java.io.File
import java.io.InputStream
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings

/**
 * Stream Downloader
 *
 * @author chaostone
 * @since 2.1
 */
trait StreamDownloader {

  def download(req: HttpServletRequest, res: HttpServletResponse, file: File): Unit = {
    download(req, res, file, file.getName)
  }

  def download(req: HttpServletRequest, res: HttpServletResponse, url: URL, fileName: String): Unit

  def download(req: HttpServletRequest, res: HttpServletResponse, file: File, fileName: String): Unit

  def download(req: HttpServletRequest, res: HttpServletResponse, is: InputStream, fileName: String): Unit
}

object StreamDownloader {
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
