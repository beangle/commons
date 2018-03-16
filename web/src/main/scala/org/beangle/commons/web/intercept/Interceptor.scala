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
package org.beangle.commons.web.intercept

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.lang.primitive.MutableInt

trait Interceptor {

  def preInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean

  def postInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit
}

trait OncePerRequestInterceptor extends Interceptor {
  final val attributeName = getClass.getName + "_count"

  override final def preInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    var count = request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (null == count) {
      count = new MutableInt
      request.setAttribute(attributeName, count)
    }
    if (count.increment() == 1) doPreInvoke(request, response) else true
  }

  def doPreInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    true
  }

  def doPostInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {

  }

  override final def postInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    var count = request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (count.decrement() == 0) doPostInvoke(request, response)
  }
}
