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
package org.beangle.commons.web.io

import java.io.{ByteArrayOutputStream, File, OutputStream}

import org.beangle.commons.lang.ClassLoaders
import org.junit.runner.RunWith
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

import javax.servlet.{ServletOutputStream, WriteListener}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

@RunWith(classOf[JUnitRunner])
class RangedWagonTest extends AnyFunSpec with Matchers {

  val wagon: Wagon = new RangedWagon

  describe("RangedWagon") {
    it("download") {
      var request = mock(classOf[HttpServletRequest])
      var response = mock(classOf[HttpServletResponse])
      when(response.getOutputStream).thenReturn(new ServletOutputStream() {
        var outputStream: OutputStream = new ByteArrayOutputStream()

        def write(b: Int): Unit = {
          outputStream.write(b)
        }

        def isReady(): Boolean = false

        def setWriteListener(writeListener: WriteListener): Unit = {}
      })
      val testDoc = ClassLoaders.getResource("download.txt").get
      wagon.copy(testDoc, request, response)
      verify(response).setHeader("Accept-Ranges", "bytes")
      val file = new File(testDoc.toURI())
      request = mock(classOf[HttpServletRequest])
      response = mock(classOf[HttpServletResponse])
      when(response.getOutputStream).thenReturn(new ServletOutputStream() {

        var outputStream: OutputStream = new ByteArrayOutputStream()

        def write(b: Int): Unit = {
          outputStream.write(b)
        }

        def isReady(): Boolean = false

        def setWriteListener(writeListener: WriteListener): Unit = {}
      })
      when(request.getHeader("Range")).thenReturn("bytes=5-12")
      wagon.copy(testDoc, request, response)
      verify(response).setStatus(206)
      verify(response).setHeader("Content-Range", "bytes 5-12/" + file.length)
    }
  }
}
