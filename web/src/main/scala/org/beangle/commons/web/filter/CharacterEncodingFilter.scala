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

import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.beangle.commons.lang.Strings
//remove if not needed
import scala.collection.JavaConversions._

class CharacterEncodingFilter extends Filter {

  protected var encoding: String = "utf-8"

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    request.setCharacterEncoding(encoding)
    chain.doFilter(request, response)
  }

  def init(filterConfig: FilterConfig) {
    val initEncoding = filterConfig.getInitParameter("encoding")
    if (Strings.isNotBlank(initEncoding)) {
      this.encoding = initEncoding
    }
  }

  def destroy() {
    encoding = null
  }
}
