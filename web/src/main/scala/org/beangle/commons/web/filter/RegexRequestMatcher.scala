/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.web.filter

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

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
class RegexRequestMatcher(pattern: Pattern, httpMethod: String) extends RequestMatcher with Logging {

  /**
   * Creates a case-sensitive {@code Pattern} instance to match against the
   * request.
   *
   * @param pattern  the regular expression to compile into a pattern.
   * @param httpMethod  the HTTP method to match. May be null to match all methods.
   */
  def this(patternStr: String, httpMethod: String, caseInsensitive: Boolean) {
    this(if (caseInsensitive) Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE) else Pattern.compile(patternStr), httpMethod)
  }

  /**
   * Performs the match of the request URL ( {@code servletPath + pathInfo + queryString}) against
   * the compiled
   * pattern.
   *
   * @param request the request to match
   * @return true if the pattern matches the URL, false otherwise.
   */
  def matches(request: HttpServletRequest): Boolean = {
    if (httpMethod != null && httpMethod != request.getMethod) {
      return false
    }
    var url = request.getServletPath
    val pathInfo = request.getPathInfo
    val query = request.getQueryString
    if (pathInfo != null || query != null) {
      val sb = new StringBuilder(url)
      if (pathInfo != null) sb.append(pathInfo)
      if (query != null) sb.append(query)
      url = sb.toString
    }
    logger.debug("Checking match of request : '{}'; against '{}'", url, pattern)
    pattern.matcher(url).matches()
  }
}
