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
package org.beangle.commons.web.filter

import java.util.regex.Pattern

import org.beangle.commons.regex.AntPathPattern
import org.beangle.commons.web.util.RequestUtils

import javax.servlet.http.HttpServletRequest

/**
 * Simple strategy to match an <tt>HttpServletRequest</tt>.
 *
 * @author chaostone
 */
trait RequestMatcher {

  /**
   * Decides whether the rule implemented by the strategy matches the supplied
   * request.
   *
   * @param request
   *          the request to check for a match
   * @return true if the request matches, false otherwise
   */
  def matches(request: HttpServletRequest): Boolean
}

/**
 * Matcher which compares a pre-defined ant-style pattern against the URL (
 * {@code servletPath + pathInfo}) of an {@code HttpServletRequest}. The query
 * string of the URL is ignored and matching is case-insensitive.
 *
 * @see AntPathMatcher
 */
class AntPathRequestMatcher(val pattern: AntPathPattern, val method: String) extends RequestMatcher {

  /**
   * Creates a matcher with the specific pattern which will match all HTTP
   * methods.
   */
  def this(patternStr: String, method: String = null) {
    this(new AntPathPattern(patternStr), method)
  }

  /**
   * Returns true if the configured pattern (and HTTP-Method) match those of
   * the supplied request.
   */
  def matches(request: HttpServletRequest): Boolean = {
    if (method != null && method != request.getMethod) false
    else {
      var url = RequestUtils.getServletPath(request)
      if (null != request.getPathInfo) url += request.getPathInfo
      pattern.matches(url)
    }
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: AntPathRequestMatcher => this.pattern == other.pattern && this.method == other.method
      case _ => false
    }
  }

  override def toString(): String = if (null == method) s"Ant [pattern='$pattern']" else s"Ant [pattern='$pattern',method=method]"
}

/**
 * Uses a regular expression to decide whether a supplied the URL of a supplied
 * {@code HttpServletRequest}. Can also be configured to match a specific HTTP
 * method. The match is performed against the {@code servletPath + pathInfo + queryString} of the
 * request and is
 * case-sensitive by default. Case-insensitive matching can be used by using the
 * constructor which takes the {@code caseInsentitive} argument.
 *
 * @author chaostone
 */
class RegexRequestMatcher(pattern: Pattern, method: String) extends RequestMatcher {

  /**
   * Creates a case-sensitive {@code Pattern} instance to match against the
   * request.
   */
  def this(patternStr: String, method: String, caseInsensitive: Boolean) {
    this(if (caseInsensitive) Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE) else Pattern.compile(patternStr), method)
  }

  /**
   * Performs the match of the request URL ( {@code servletPath + pathInfo + queryString}) against
   * the compiled pattern.
   */
  def matches(request: HttpServletRequest): Boolean = {
    if (method != null && method != request.getMethod) false
    else {
      var url = RequestUtils.getServletPath(request)
      val pathInfo = request.getPathInfo
      val query = request.getQueryString
      if (pathInfo != null || query != null) {
        val sb = new StringBuilder(url)
        if (pathInfo != null) sb.append(pathInfo)
        if (query != null) sb.append(query)
        url = sb.toString
      }
      pattern.matcher(url).matches()
    }
  }
}
