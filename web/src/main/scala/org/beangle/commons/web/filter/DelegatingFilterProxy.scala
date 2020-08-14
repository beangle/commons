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

import org.beangle.commons.lang.Throwables

import jakarta.servlet.{ Filter, FilterChain, ServletException, ServletRequest, ServletResponse }

/**
 * Proxy for a standard Servlet 5.0 Filter, delegating to a managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code web.xml}, specifying the name of the
 * target bean in the application context.
 *
 * @author chaostone
 */
class DelegatingFilterProxy(delegate: Filter) extends GenericHttpFilter {

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    delegate.doFilter(request, response, chain)
  }

  override def destroy(): Unit = {
    if (delegate != null) delegate.destroy()
  }

}
