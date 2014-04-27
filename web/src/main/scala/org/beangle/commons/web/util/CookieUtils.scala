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
package org.beangle.commons.web.util

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.logging.Logging

object CookieUtils extends Logging {

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
    var returnCookie: Cookie = null
    if (cookies == null) {
      return returnCookie
    }
    for (i <- 0 until cookies.length; if (null == returnCookie)) {
      val thisCookie = cookies(i)
      if (thisCookie.getName == name && thisCookie.getValue != "") {
        returnCookie = thisCookie
      }
    }
    returnCookie
  }

  /**
   * Convenience method to set a cookie <br>
   * 刚方法自动将value进行编码存储
   */
  def addCookie(request: HttpServletRequest,
    response: HttpServletResponse,
    name: String,
    value: String,
    path: String,
    age: Int) {
    debug(s"add cookie[name:$name,value=$value,path=$path]")
    var cookie: Cookie = null
    cookie = new Cookie(name, URLEncoder.encode(value, "utf-8"))
    cookie.setSecure(false)
    cookie.setPath(path)
    cookie.setMaxAge(age)
    response.addCookie(cookie)
  }

  /**
   * 默认按照应用上下文进行设置
   */
  def addCookie(request: HttpServletRequest,
    response: HttpServletResponse,
    name: String,
    value: String,
    age: Int) {
    val contextPath = if (!request.getContextPath.endsWith("/")) request.getContextPath + "/" else request.getContextPath
    addCookie(request, response, name, value, contextPath, age)
  }

  def deleteCookieByName(request: HttpServletRequest, response: HttpServletResponse, name: String) {
    deleteCookie(response, getCookie(request, name), "")
  }

  /**
   * Convenience method for deleting a cookie by name
   */
  def deleteCookie(response: HttpServletResponse, cookie: Cookie, path: String) {
    if (cookie != null) {
      cookie.setMaxAge(0)
      cookie.setPath(path)
      response.addCookie(cookie)
    }
  }
}
