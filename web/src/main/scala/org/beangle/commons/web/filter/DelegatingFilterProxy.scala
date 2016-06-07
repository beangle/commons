/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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

import org.beangle.commons.inject.{ Container, ContainerListener }
import org.beangle.commons.lang.Throwables

import javax.servlet.{ Filter, FilterChain, ServletException, ServletRequest, ServletResponse }

/**
 * Proxy for a standard Servlet 2.3 Filter, delegating to a managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code web.xml}, specifying the name of the
 * target bean in the application context.
 *
 * @author chaostone
 */
class DelegatingFilterProxy extends GenericHttpFilter with ContainerListener {

  private var delegate: Filter = _

  var beanName: String = _

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    delegate.doFilter(request, response, chain)
  }

  override def onStarted(container: Container) {
    try {
      this.delegate = initDelegate(container)
    } catch {
      case e: ServletException => Throwables.propagate(e)
    }
  }

  override def init() {
    if (null == beanName) beanName = filterName
    val wac = Container.ROOT
    if (wac != null) delegate = initDelegate(wac) else Container.addListener(this)
  }

  protected def initDelegate(container: Container): Filter = {
    container.getBean[Filter](beanName) match {
      case Some(filter) => {
        filter.init(filterConfig); filter
      }
      case None => throw new RuntimeException(s"Cannot find $beanName in context.")
    }
  }

  override def requiredProperties: Set[String] = Set("beanName")

  override def destroy() {
    if (delegate != null) delegate.destroy()
  }

}
