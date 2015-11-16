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