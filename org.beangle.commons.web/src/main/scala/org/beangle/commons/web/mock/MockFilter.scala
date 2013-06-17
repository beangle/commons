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
package org.beangle.commons.web.mock

import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * A simple filter that the test case can delegate to.
 *
 * @author chaostone
 * @version $Id: MockFilter.java 2217 2007-10-27 00:45:30Z $
 */
class MockFilter extends Filter {

  var destroyed: Boolean = false

  var doFiltered: Boolean = false

  var initialized: Boolean = false

  def destroy() {
    destroyed = true
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    doFiltered = true
    chain.doFilter(request, response)
  }

  def init(config: FilterConfig) {
    initialized = true
  }
}
