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

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlBuilder

import javax.servlet.http.{ Cookie, HttpServletRequest, HttpServletResponse }

/**
 * @author chaostone
 */
class CookieGenerator(val name: String) {
  var domain: String = _
  var path: String = _
  var secure: Boolean = _
  var httpOnly: Boolean = true
  var maxAge: Int = -1
  var port: Int = 80

  def addCookie(request: HttpServletRequest, response: HttpServletResponse, value: String): Unit = {
    val cookie = createCookie(request, value)
    cookie.setMaxAge(maxAge)
    cookie.setSecure(secure)
    cookie.setHttpOnly(httpOnly)
    response.addCookie(cookie)
  }

  def removeCookie(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val cookie = createCookie(request, "")
    cookie.setMaxAge(0)
    response.addCookie(cookie);
  }

  protected def createCookie(request: HttpServletRequest, value: String): Cookie = {
    val cookie = new Cookie(name, value);
    if (domain != null && request.getServerName != domain) {
      cookie.setDomain(domain)
    }
    cookie.setPath(path)
    cookie
  }

  def base_=(baseUrl: String): Unit = {
    var b = Strings.trim(baseUrl)
    if (baseUrl.contains("https://")) {
      this.secure = true
      b = Strings.replace(b, "https://", "")
    } else {
      b = Strings.replace(b, "http://", "")
    }
    val pathIdx = b.indexOf('/')
    if (-1 == pathIdx) {
      this.path = "/"
    } else {
      this.path = b.substring(pathIdx)
      b = b.substring(0, pathIdx)
    }
    val portIdx = b.indexOf(':')

    if (-1 == portIdx) {
      if (secure) port = 443
    } else {
      port = Integer.parseInt(b.substring(portIdx + 1))
      b = b.substring(0, portIdx)
    }
    this.domain = b
  }

  def base: String = {
    val b = new UrlBuilder("")
    b.setServletPath(this.path)
    b.setServerName(domain)
    b.setPort(port)
    b.setScheme(if (secure) "https" else "http")
    b.buildUrl()
  }
}
