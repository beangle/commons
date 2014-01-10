/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.util

import javax.servlet.http.HttpServletRequest
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers

class RequestUtilsTest extends FunSpec with Matchers {

  describe("RequestUtils") {
    it("testGetServletPath") {
      var request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("/")
      when(request.getRequestURI).thenReturn("/")
      RequestUtils.getServletPath(request)
      request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("/")
      when(request.getRequestURI).thenReturn("/demo")
      RequestUtils.getServletPath(request)
      request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("")
      when(request.getRequestURI).thenReturn("/demo")
      RequestUtils.getServletPath(request)
    }
  }
}
