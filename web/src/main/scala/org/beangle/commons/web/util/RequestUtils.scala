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

import java.net.URLEncoder
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.codec.net.BCoder
import org.beangle.commons.http.agent._
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

object RequestUtils extends Logging {

  /**
   * Returns remote ip address.
   * <ul>
   * <li>First,it lookup request header("x-forwarded-for"->"Proxy-Client-IP"->"WL-Proxy-Client-IP")
   * <li>Second,invoke request.getRemoteAddr()
   * </ul>
   *
   * @param request
   */
  def getIpAddr(request: HttpServletRequest): String = {
    var ip = request.getHeader("x-forwarded-for")
    if (ip == null || ip.length == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP")
    }
    if (ip == null || ip.length == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP")
    }
    if (ip == null || ip.length == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr
    }
    ip
  }

  /**
   * Return the true servlet path.
   * When servletPath provided by container is empty,It will return requestURI'
   * <p>
   * 查找当前调用的action对应的.do<br>
   * 例如http://localhost/myapp/dd.do 返回/dd.do<br>
   * http://localhost/myapp/dir/to/dd.do 返回/dir/to/dd.do
   */
  def getServletPath(request: HttpServletRequest): String = {
    var servletPath = request.getServletPath
    if (Strings.isNotEmpty(servletPath)) {
      servletPath
    } else {
      val uri = request.getRequestURI
      if (uri.length == 1) return ""
      var context = request.getContextPath
      val length = context.length
      if (length > 2) {
        if ('/' == context.charAt(length - 1)) context = context.substring(0, length - 1)
        servletPath = uri.substring(context.length)
        servletPath
      } else {
        uri
      }
    }
  }

  def getRealPath(servletContext: ServletContext, path: String): String = {
    val realPath = if (!path.startsWith("/")) servletContext.getRealPath("/" + path) else servletContext.getRealPath(path)
    if (realPath == null) {
      throw new RuntimeException("ServletContext resource [" + path + "] cannot be resolved to absolute file path - " +
        "web application archive not expanded?")
    }
    realPath
  }

  def encodeAttachName(request: HttpServletRequest, attach_name: String): String = {
    val agent = request.getHeader("USER-AGENT")
    var newName = attach_name
    try {
      newName = if (null != agent && -1 != agent.indexOf("MSIE")) URLEncoder.encode(attach_name, "UTF-8") else new BCoder().encode(attach_name)
    } catch {
      case e: Exception => {
        error("cannot encode " + attach_name, e)
        return attach_name
      }
    }
    newName
  }

  /**
   * Return {@code Useragent} of request.
   *
   * @param request
   */
  def getUserAgent(request: HttpServletRequest): Useragent = {
    val head = request.getHeader("USER-AGENT")
    val agent = new Useragent(getIpAddr(request), Browser.parse(head), Os.parse(head))
    if (agent.os == Oss.Unknown || agent.browser == Browsers.Unknown) {
      info("Cannot parser user agent:" + request.getHeader("USER-AGENT"))
    }
    agent
  }
}
