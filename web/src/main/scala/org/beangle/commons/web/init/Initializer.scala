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
package org.beangle.commons.web.init

import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletContextListener

trait Initializer {
  var boss: BootstrapListener = _
  /**
   * Configure the given {@link ServletContext} with any servlets, filters, listeners
   * context-params and attributes necessary for initializing this web application.
   *
   * @param servletContext the {@code ServletContext} to initialize
   */
  @throws(classOf[ServletException])
  def onStartup(servletContext: ServletContext)

  final def addListener(other: ServletContextListener) {
    boss.addListener(other)
  }
}
