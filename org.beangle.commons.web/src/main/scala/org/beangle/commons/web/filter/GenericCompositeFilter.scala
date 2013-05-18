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

import java.io.IOException
import java.util.List
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
//remove if not needed
import scala.collection.JavaConversions._

object GenericCompositeFilter {

  /**
   * A <code>FilterChain</code> that records whether or not
   * {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} is
   * called.
   */
  protected class VirtualFilterChain(chain: FilterChain, val additionalFilters: List[_ <: Filter])
      extends FilterChain {

    private val originalChain = chain

    private var currentPosition: Int = 0

    def doFilter(request: ServletRequest, response: ServletResponse) {
      if (currentPosition == additionalFilters.size) {
        originalChain.doFilter(request, response)
      } else {
        currentPosition += 1
        additionalFilters.get(currentPosition - 1).doFilter(request, response, this)
      }
    }
  }
}
