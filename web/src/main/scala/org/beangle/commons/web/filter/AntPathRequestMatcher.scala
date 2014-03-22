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
package org.beangle.commons.web.filter

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.regex.AntPathPattern

/**
 * Matcher which compares a pre-defined ant-style pattern against the URL (
 * {@code servletPath + pathInfo}) of an {@code HttpServletRequest}. The query
 * string of the URL is ignored and matching is case-insensitive.
 *
 * @see AntPathMatcher
 */
class AntPathRequestMatcher(val pattern: AntPathPattern, val httpMethod: String) extends RequestMatcher {

  /**
   * Creates a matcher with the specific pattern which will match all HTTP
   * methods.
   *
   * @param pattern
   *          the ant pattern to use for matching
   */
  def this(patternStr: String, httpMethod: String = null) {
    this(new AntPathPattern(patternStr), httpMethod)
  }

  /**
   * Returns true if the configured pattern (and HTTP-Method) match those of
   * the supplied request.
   *
   * @param request
   *          the request to match against. The ant pattern will be matched
   *          against the {@code servletPath} + {@code pathInfo} of the
   *          request.
   */
  def matches(request: HttpServletRequest): Boolean = {
    if (httpMethod != null && httpMethod != request.getMethod) {
      return false
    }
    var url = request.getServletPath
    if (null != request.getPathInfo) url += request.getPathInfo
    pattern.matches(url)
  }

  override def equals(obj: Any): Boolean = {
    if (!(obj.isInstanceOf[AntPathRequestMatcher])) {
      return false
    }
    val other = obj.asInstanceOf[AntPathRequestMatcher]
    this.pattern == other.pattern && this.httpMethod == other.httpMethod
  }

  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append("Ant [pattern='").append(pattern).append("'")
    if (httpMethod != null) sb.append(", " + httpMethod)
    sb.append("]")
    sb.toString
  }
}
