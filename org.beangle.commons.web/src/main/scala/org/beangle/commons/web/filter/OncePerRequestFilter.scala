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

import javax.servlet.ServletException
import javax.servlet.ServletRequest
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Once per request filter.
 *
 * @author chaostone
 * @since 3.0.0
 */
abstract class OncePerRequestFilter extends GenericHttpFilter {

  private var filteredAttributeName: String = _

  def firstEnter(request: ServletRequest): Boolean = {
    if (null != request.getAttribute(filteredAttributeName)) false else {
      request.setAttribute(filteredAttributeName, true)
      true
    }
  }

  protected override def initFilterBean() {
    var name = getFilterName
    if (name == null) name = getClass.getName
    filteredAttributeName = name + ".FILTED"
  }
}
