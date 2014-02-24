/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.io

import java.io.File
import java.io.InputStream
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Stream Downloader
 *
 * @author chaostone
 * @since 2.1
 */
trait StreamDownloader {

  def download(req: HttpServletRequest, res: HttpServletResponse, file: File): Unit

  def download(req: HttpServletRequest,
    res: HttpServletResponse,
    url: URL,
    display: String): Unit

  def download(req: HttpServletRequest,
    res: HttpServletResponse,
    file: File,
    display: String): Unit

  def download(req: HttpServletRequest,
    res: HttpServletResponse,
    inStream: InputStream,
    name: String,
    display: String): Unit
}