/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.web.servlet

import org.beangle.commons.inject.{ Container, ContainerListener }
import org.beangle.commons.lang.Throwables
import javax.servlet.ServletException
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class DelegatingServletProxy extends HttpServlet with ContainerListener {

  private var delegate: HttpServlet = _

  protected var targetBeanName: String = _

  override def init() {
    if (null == targetBeanName) targetBeanName = getServletName()
    val wac = Container.ROOT
    if (wac == null) Container.addListener(this)
    else delegate = initDelegate(wac)
  }

  protected override def service(req: ServletRequest, resp: ServletResponse) {
    delegate.service(req, resp);
  }

  override def onStarted(container: Container) {
    try {
      delegate = initDelegate(container)
    } catch {
      case e: ServletException => Throwables.propagate(e)
    }
  }

  override def destroy() {
    if (delegate != null) delegate.destroy()
  }

  protected def initDelegate(container: Container): HttpServlet = {
    container.getBean[HttpServlet](targetBeanName) match {
      case Some(servlet) =>
        servlet.init(getServletConfig()); servlet
      case None => throw new RuntimeException("Cannot find " + targetBeanName + " in context.")
    }
  }
}