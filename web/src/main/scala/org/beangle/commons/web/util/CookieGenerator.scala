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
package org.beangle.commons.web.util

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie

/**
 * @author chaostone
 */
class CookieGenerator(name: String) {
  var domain: String = _
  var path: String = _
  var secure: Boolean = _
  var httpOnly: Boolean = true
  var maxAge: Int = -1

  def addCookie(response: HttpServletResponse, value: String): Unit = {
    val cookie = createCookie(value)
    cookie.setMaxAge(maxAge)
    cookie.setSecure(secure)
    cookie.setHttpOnly(httpOnly)
    response.addCookie(cookie)
  }

  def removeCookie(response: HttpServletResponse): Unit = {
    val cookie = createCookie("")
    cookie.setMaxAge(0)
    response.addCookie(cookie);
  }

  protected def createCookie(value: String): Cookie = {
    val cookie = new Cookie(name, value);
    if (domain != null) cookie.setDomain(domain)
    cookie.setPath(path)
    cookie
  }
}