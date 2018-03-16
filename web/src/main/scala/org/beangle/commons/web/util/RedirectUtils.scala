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
package org.beangle.commons.web.util

import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings

object RedirectUtils {

  def sendRedirect(request: HttpServletRequest, response: HttpServletResponse, url: String) {
    if (!url.startsWith("http")) {
      val cxtPath = request.getContextPath
      val redirectUrl = response.encodeRedirectURL((if (cxtPath == "/") "" else (cxtPath)) + url)
      response.sendRedirect(redirectUrl)
    } else {
      response.sendRedirect(url)
    }
  }

  def isValidRedirectUrl(url: String): Boolean = {
    Strings.isBlank(url) || url.startsWith("/") || url.toLowerCase().startsWith("http")
  }
}
