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

import java.net.{URLDecoder, URLEncoder}

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

object CookieUtils {

  def getCookieValue(cookie: Cookie): String = {
    try {
      URLDecoder.decode(cookie.getValue, "utf-8")
    } catch {
      case e: Exception => null
    }
  }

  /**
    * 获取cookie中的value<br>
    * 自动负责解码<br>
    */
  def getCookieValue(request: HttpServletRequest, cookieName: String): String = {
    try {
      val cookie = getCookie(request, cookieName)
      if (null == cookie) {
        null
      } else {
        URLDecoder.decode(cookie.getValue, "utf-8")
      }
    } catch {
      case e: Exception => null
    }
  }

  /**
    * Convenience method to get a cookie by name
    */
  def getCookie(request: HttpServletRequest, name: String): Cookie = {
    val cookies = request.getCookies
    if (cookies == null) return null

    var returnCookie: Cookie = null
    var i = 0
    while (i < cookies.length && null == returnCookie) {
      val thisCookie = cookies(i)
      if (thisCookie.getName == name && thisCookie.getValue != "") returnCookie = thisCookie
      i += 1
    }
    returnCookie
  }

  /**
    * Convenience method to set a cookie <br>
    * 刚方法自动将value进行编码存储
    */
  def addCookie(request: HttpServletRequest, response: HttpServletResponse,
                name: String, value: String, path: String, age: Int): Unit = {
    val cookie = new Cookie(name, URLEncoder.encode(value, "utf-8"))
    cookie.setSecure(RequestUtils.isHttps(request))
    cookie.setPath(path)
    cookie.setMaxAge(age)
    cookie.setHttpOnly(true)
    response.addCookie(cookie)
  }

  /**
    * 默认按照应用上下文进行设置
    */
  def addCookie(request: HttpServletRequest, response: HttpServletResponse,
                name: String, value: String, age: Int): Unit = {
    val path = getDefaultCookiePath(request)
    addCookie(request, response, name, value, path, age)
  }

  def deleteCookieByName(request: HttpServletRequest, response: HttpServletResponse, name: String): Boolean = {
    val path = getDefaultCookiePath(request)
    deleteCookie(response, getCookie(request, name), path)
  }


  /** Convenience method for deleting a cookie by name
    *
    */
  def deleteCookie(response: HttpServletResponse, cookie: Cookie, path: String): Boolean = {
    if (cookie != null) {
      cookie.setMaxAge(0)
      cookie.setPath(path)
      response.addCookie(cookie)
      true
    } else {
      false
    }
  }

  /** Clean all session cookies
    * @param request
    * @param response
    */
  def clearSession(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val cookies = request.getCookies
    if (cookies != null) {
      val contextPath = getDefaultCookiePath(request)
      var i = 0
      while (i < cookies.length) {
        val c = cookies(i)
        if (c.getMaxAge < 0) {
          val domain = c.getDomain
          if (null == domain || domain == request.getServerName) {
            deleteCookie(response, c, contextPath)
          }
        }
        i += 1
      }
    }
  }

  private def getDefaultCookiePath(request: HttpServletRequest): String = {
    if (!request.getContextPath.endsWith("/")) request.getContextPath + "/" else request.getContextPath
  }
}
