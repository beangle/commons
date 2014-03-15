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
package org.beangle.commons.web.io

import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.codec.net.BCoder
import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.lang.ClassLoaders
import org.scalatest.FunSpec
import org.scalatest.Matchers
import javax.servlet.WriteListener
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefaultStreamDownloaderTest extends FunSpec with Matchers {

  val streamDownloader: StreamDownloader = new DefaultStreamDownloader(new MimeTypeProvider())

  describe("DefaultStreamDownloader") {
    it("download") {
      val request = mock(classOf[HttpServletRequest])
      val response = mock(classOf[HttpServletResponse])
      when(response.getOutputStream).thenReturn(new ServletOutputStream() {
        val outputStream: OutputStream = new ByteArrayOutputStream()
        def write(b: Int) {
          outputStream.write(b)
        }
        def isReady() = false

        def setWriteListener(writeListener: WriteListener) {}
      })
      val testDoc = ClassLoaders.getResource("download.txt", getClass)
      streamDownloader.download(request, response, testDoc, null)
    }

    it("encode/decode") {
      val value = "汉字-english and .;"
      val ecodedValue = URLEncoder.encode(value, "utf-8")
      URLDecoder.decode(ecodedValue, "utf-8") should equal(value)
    }

    it("Bcoder encode/decode") {
      val value = "汉字-english and .;"
      val encodedValue = new BCoder().encode(value)
      new BCoder().decode(encodedValue) should equal(value)
    }
  }
}
