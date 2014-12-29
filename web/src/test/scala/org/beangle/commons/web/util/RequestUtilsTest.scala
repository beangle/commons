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
package org.beangle.commons.web.util

import org.junit.runner.RunWith
import org.mockito.Mockito.{ mock, when }
import org.scalatest.{ FunSpec, Matchers }
import javax.servlet.http.HttpServletRequest
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RequestUtilsTest extends FunSpec with Matchers {

  describe("RequestUtils") {
    it("testGetServletPath") {
      var request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("/")
      when(request.getRequestURI).thenReturn("/")
      assert("" == RequestUtils.getServletPath(request))

      request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("/")
      when(request.getRequestURI).thenReturn("/demo;jsessoin_id=1")
      assert("/demo" == RequestUtils.getServletPath(request))

      request = mock(classOf[HttpServletRequest])
      when(request.getContextPath).thenReturn("")
      when(request.getRequestURI).thenReturn("/demo")
      assert("/demo" == RequestUtils.getServletPath(request))
    }
  }
}
