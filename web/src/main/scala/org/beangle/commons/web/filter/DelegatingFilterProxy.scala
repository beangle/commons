/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.beangle.commons.inject.Container
import org.beangle.commons.inject.ContainerHook
import org.beangle.commons.inject.Containers
import org.beangle.commons.lang.Throwables

/**
 * Proxy for a standard Servlet 2.3 Filter, delegating to a managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code web.xml}, specifying the name of the
 * target bean in the application context.
 *
 * @author chaostone
 */
class DelegatingFilterProxy extends GenericHttpFilter with ContainerHook {

  private var delegate: Filter = _

  protected var targetBeanName: String = _

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    delegate.doFilter(request, response, chain)
  }

  def notify(container: Container) {
    try {
      setDelegate(initDelegate(container))
    } catch {
      case e: ServletException => Throwables.propagate(e)
    }
  }

  protected override def initFilterBean() {
    if (null == targetBeanName) targetBeanName = getFilterName
    val wac = Containers.root
    if (wac != null) delegate = initDelegate(wac) else Containers.addHook(this)
  }

  override def destroy() {
    if (delegate != null) delegate.destroy()
  }

  protected def initDelegate(container: Container): Filter = {
    container.getBean[Filter](targetBeanName) match {
      case Some(filter) => {
        filter.init(filterConfig); filter
      }
      case None => throw new RuntimeException("Cannot find " + targetBeanName + " in context.")
    }
  }

  def setDelegate(delegate: Filter) {
    this.delegate = delegate
  }
}
