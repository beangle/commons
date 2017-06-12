/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.web.io

import java.io.{ ByteArrayOutputStream, File, OutputStream }

import org.beangle.commons.lang.ClassLoaders
import org.junit.runner.RunWith
import org.mockito.Mockito.{ mock, verify, when }
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

import javax.servlet.{ ServletOutputStream, WriteListener }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

@RunWith(classOf[JUnitRunner])
class RangedWagonTest extends FunSpec with Matchers {

  val wagon: Wagon = new RangedWagon

  describe("RangedWagon") {
    it("download") {
      var request = mock(classOf[HttpServletRequest])
      var response = mock(classOf[HttpServletResponse])
      when(response.getOutputStream).thenReturn(new ServletOutputStream() {
        var outputStream: OutputStream = new ByteArrayOutputStream()
        def write(b: Int) {
          outputStream.write(b)
        }
        def isReady() = false

        def setWriteListener(writeListener: WriteListener) {}
      })
      val testDoc = ClassLoaders.getResource("download.txt").get
      wagon.copy(testDoc, request, response)
      verify(response).setHeader("Accept-Ranges", "bytes")
      val file = new File(testDoc.toURI())
      request = mock(classOf[HttpServletRequest])
      response = mock(classOf[HttpServletResponse])
      when(response.getOutputStream).thenReturn(new ServletOutputStream() {

        var outputStream: OutputStream = new ByteArrayOutputStream()

        def write(b: Int) {
          outputStream.write(b)
        }
        def isReady() = false

        def setWriteListener(writeListener: WriteListener) {}
      })
      when(request.getHeader("Range")).thenReturn("bytes=5-12")
      wagon.copy(testDoc, request, response)
      verify(response).setStatus(206)
      verify(response).setHeader("Content-Range", "bytes 5-12/" + file.length)
    }
  }
}
