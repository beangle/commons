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
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

object GenericCompositeFilter {

  /**
   * A <code>FilterChain</code> that records whether or not
   * {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} is
   * called.
   */
  protected class VirtualFilterChain(val originalChain: FilterChain, additionalFilters: List[_ <: Filter])
      extends FilterChain {

    private val iter: Iterator[_ <: Filter] = additionalFilters.iterator

    def doFilter(request: ServletRequest, response: ServletResponse) {
      if (iter.hasNext) iter.next.doFilter(request, response, this)
      else originalChain.doFilter(request, response)
    }
  }
}