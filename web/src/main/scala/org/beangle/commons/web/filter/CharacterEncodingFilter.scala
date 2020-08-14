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

import java.io.IOException
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.beangle.commons.lang.Strings

class CharacterEncodingFilter extends GenericHttpFilter {

  var encoding: String = "utf-8"
  var forceEncoding = false

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    request.setCharacterEncoding(encoding)
    if (forceEncoding) response.setCharacterEncoding(encoding)
    chain.doFilter(request, response)
  }
}
