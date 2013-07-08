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
package org.beangle.commons.web.access

import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.web.filter.GenericHttpFilter
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Access monitor filter
 *
 * @author chaostone
 * @since 3.0.1
 */
class AccessMonitorFilter extends GenericHttpFilter {

  var accessMonitor: AccessMonitor = _

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    var ar: AccessRequest = null
    try {
      ar = accessMonitor.begin(request.asInstanceOf[HttpServletRequest])
      chain.doFilter(request, response)
    } finally {
      accessMonitor.end(ar, response.asInstanceOf[HttpServletResponse])
    }
  }

  def setAccessMonitor(accessMonitor: AccessMonitor) {
    this.accessMonitor = accessMonitor
  }
}
