/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.web.url

import javax.servlet.http.HttpServletRequest

object UrlBuilder {
  def url(req: HttpServletRequest): String = {
    val builder = new UrlBuilder(req.getContextPath())
    builder.setScheme(req.getScheme).setServerName(req.getServerName).setPort(req.getServerPort)
      .setQueryString(req.getQueryString)
    builder.buildUrl()
  }
}
/**
 * @author chaostone
 */
class UrlBuilder(cxtPath: String) {

  var scheme: String = _

  var serverName: String = _

  var port: Int = _

  var contextPath: String = if (cxtPath == "/") "" else cxtPath

  var servletPath: String = _

  var requestURI: String = _

  var pathInfo: String = _

  var queryString: String = _

  /**
   * Returns servetPath without contextPath
   */
  private def buildServletPath(): String = {
    var uri = servletPath
    if (uri == null && null != requestURI) {
      uri = requestURI
      if (contextPath != "") uri = uri.substring(contextPath.length)
    }
    if ((null == uri)) "" else uri
  }

  /**
   * Returns request Url contain pathinfo and queryString but without contextPath.
   */
  def buildRequestUrl(): String = {
    val sb = new StringBuilder()
    sb.append(buildServletPath())
    if (null != pathInfo) sb.append(pathInfo)
    if (null != queryString) sb.append('?').append(queryString)
    sb.toString
  }

  /**
   * Returns full url
   */
  def buildUrl(): String = {
    val sb = new StringBuilder()
    var includePort = true
    if (null != scheme) {
      sb.append(scheme).append("://")
      includePort = (port != (if (scheme == "http") 80 else 443))
    }
    if (null != serverName) {
      sb.append(serverName)
      if (includePort && port > 0) {
        sb.append(':').append(port)
      }
    }
    sb.append(contextPath)
    sb.append(buildRequestUrl())
    sb.toString
  }

  def setScheme(scheme: String): this.type = {
    this.scheme = scheme
    this
  }

  def setServerName(serverName: String): this.type = {
    this.serverName = serverName
    this
  }

  def setPort(port: Int): this.type = {
    this.port = port
    this
  }

  /**
   * ContextPath should start with / but not ended with /
   */
  def setContextPath(contextPath: String): this.type = {
    this.contextPath = contextPath
    this
  }

  /**
   * Set servletPath ,start with /
   */
  def setServletPath(servletPath: String): this.type = {
    this.servletPath = servletPath
    this
  }

  /**
   * Set requestURI ,it should start with /
   */
  def setRequestURI(requestURI: String): this.type = {
    this.requestURI = requestURI
    this
  }

  def setPathInfo(pathInfo: String): this.type = {
    this.pathInfo = pathInfo
    this
  }

  def setQueryString(queryString: String): this.type = {
    this.queryString = queryString
    this
  }
}
