/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.lang.ClassLoaders
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class SplitStreamDownloaderTest {

  var streamDownloader: StreamDownloader = new SplitStreamDownloader(new MimeTypeProvider())

  def download() {
    var request = mock(classOf[HttpServletRequest])
    var response = mock(classOf[HttpServletResponse])
    when(response.getOutputStream).thenReturn(new ServletOutputStream() {

      var outputStream: OutputStream = new ByteArrayOutputStream()

      def write(b: Int) {
        outputStream.write(b)
      }
    })
    val testDoc = ClassLoaders.getResource("download.txt", getClass)
    streamDownloader.download(request, response, testDoc, null)
    verify(response).setHeader("Accept-Ranges", "bytes")
    val file = new File(testDoc.toURI())
    request = mock(classOf[HttpServletRequest])
    response = mock(classOf[HttpServletResponse])
    when(response.getOutputStream).thenReturn(new ServletOutputStream() {

      var outputStream: OutputStream = new ByteArrayOutputStream()

      def write(b: Int) {
        outputStream.write(b)
      }
    })
    when(request.getHeader("Range")).thenReturn("bytes=5-12")
    streamDownloader.download(request, response, testDoc, null)
    verify(response).setStatus(206)
    verify(response).setHeader("Content-Range", "bytes 5-12/" + file.length)
  }
}
