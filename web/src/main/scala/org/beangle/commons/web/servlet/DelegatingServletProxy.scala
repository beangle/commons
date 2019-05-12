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
package org.beangle.commons.web.servlet

import javax.servlet.{ServletRequest, ServletResponse}
import javax.servlet.http.HttpServlet

class DelegatingServletProxy(delegate: HttpServlet) extends HttpServlet {

  protected override def service(req: ServletRequest, resp: ServletResponse): Unit = {
    delegate.service(req, resp)
  }

  override def destroy(): Unit = {
    if (delegate != null) delegate.destroy()
  }

}
