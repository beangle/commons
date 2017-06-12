/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.web.resource.filter

import org.beangle.commons.web.resource.ResourceFilter
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.web.resource.ProcessContext
import org.beangle.commons.web.resource.ProcessChain
import java.net.URL
import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.util.Calendar

class HeaderFilter extends ResourceFilter {
  /**
   * Static resource expire 7 days by default
   */
  var expireDays = 7

  override def filter(context: ProcessContext, request: HttpServletRequest, response: HttpServletResponse,
    chain: ProcessChain) {
    // Get max last modified time stamp.
    var maxLastModified = -1
    for (res <- context.resources) {
      val lastModified0 = lastModified(res.url)
      if (lastModified0 > maxLastModified) maxLastModified = lastModified0.intValue()
    }
    val requestETag = request.getHeader("If-None-Match")
    val newETag = String.valueOf(maxLastModified)
    response.setHeader("ETag", newETag)
    // not modified, content is not sent - only basic headers and status SC_NOT_MODIFIED
    if (newETag.equals(requestETag)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
      return
    } else {
      chain.process(context, request, response)
      // set heading information for caching static content
      val cal = Calendar.getInstance()
      cal.add(Calendar.DAY_OF_MONTH, expireDays)
      val expires = cal.getTimeInMillis()
      response.setDateHeader("Date", System.currentTimeMillis())
      response.setDateHeader("Expires", expires)
      response.setDateHeader("Retry-After", expires)
      response.setHeader("Cache-Control", "public")
      if (maxLastModified > 0) response.setDateHeader("Last-Modified", maxLastModified)
    }
  }

  /**
   * Return url's last modified date time.
   * saves some opening and closing
   */
  private def lastModified(url: URL): Long = {
    if (url.getProtocol().equals("file")) {
      return new File(url.getFile()).lastModified
    } else {
      try {
        val conn = url.openConnection()
        conn match {
          case jarConn: JarURLConnection =>
            val jarURL = jarConn.getJarFileURL();
            if (jarURL.getProtocol().equals("file")) new File(jarURL.getFile()).lastModified() else -1
          case _ => -1
        }
      } catch {
        case e: IOException => -1
      }
    }
  }

}