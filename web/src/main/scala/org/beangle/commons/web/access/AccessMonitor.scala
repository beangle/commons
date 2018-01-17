/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author chaostone
 * @since 3.0.1
 */
trait AccessMonitor {

  /**
   * Return access request content;
   *
   * @param request the servlet request
   */
  def begin(request: HttpServletRequest): AccessRequest

  /**
   * finish this access
   */
  def end(request: AccessRequest, response: HttpServletResponse): Unit

  /**
   * Return current request list.
   */
  def snapshot: List[AccessRequest]
}
