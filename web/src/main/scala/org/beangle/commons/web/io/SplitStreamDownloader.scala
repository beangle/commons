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

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.Stopwatch

/**
 * SplitStreamDownloader
 * <p>
 * Split download senario like this:
 * <li>Server first response:200</li>
 *
 * <pre>
 * Content-Length=106786028
 * Accept-Ranges=bytes
 * </pre>
 *
 * <li>Client send request :</li>
 *
 * <pre>
 * Range: bytes=2000070-106786027
 * </pre>
 *
 * <li>Server send next response:206</li>
 *
 * <pre>
 * Content-Length=106786028
 * Content-Range=bytes 2000070-106786027/106786028
 * </pre>
 *
 * @author chaostone
 * @since 2.4
 */
class SplitStreamDownloader(mimeTypeProvider: MimeTypeProvider) extends DefaultStreamDownloader(mimeTypeProvider) {

  override def download(request: HttpServletRequest,
    response: HttpServletResponse,
    input: InputStream,
    name: String,
    display: String) {
    val attach = getAttachName(name, display)
    response.reset()
    addContent(request, response, attach)
    response.setHeader("Accept-Ranges", "bytes")
    response.setHeader("connection", "Keep-Alive")
    var length = 0
    var start = 0L
    var begin = 0L
    var stop = 0L
    val watch = new Stopwatch(true)
    try {
      length = input.available()
      stop = length - 1
      response.setContentLength(length)
      val rangestr = request.getHeader("Range")
      if (null != rangestr) {
        val readlength = Strings.substringAfter(rangestr, "bytes=").split("-")
        start = java.lang.Long.parseLong(readlength(0))
        if (readlength.length > 1 && Strings.isNotEmpty(readlength(1))) {
          stop = java.lang.Long.parseLong(readlength(1))
        }
        if (start != 0) {
          response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
          val crange = "bytes " + start + "-" + stop + "/" + length
          response.setHeader("Content-Range", crange)
        }
      }
      val output = response.getOutputStream
      input.skip(start)
      begin = start
      val size = 4 * 1024
      val buffer = Array.ofDim[Byte](size)
      var step = maxStep(start, stop, size)
      while (step > 0) {
        val readed = input.read(buffer, 0, step)
        if (readed == -1) //break
          output.write(buffer, 0, readed)
        start += readed
        step = maxStep(start, stop, size)
      }
    } catch {
      case e: IOException =>
      case e: Exception => warn(s"download file error $attach" , e)
    } finally {
      IOs.close(input)
      if (debugEnabled) {
        var percent = if (length == 0) "100%" else (((start - begin) * 1.0 / length) * 10000).toInt / 100.0f + "%"
        val time = watch.elapsedMillis
        var rate = if (start - begin > 0) (((start - begin) * 1.0 / time * 1000) / 1024).toInt else 0
        debug(s"$attach($begin-$stop/$length) download ${start - begin}[$percent] in $time ms with $rate KB/s")
      }
    }
  }

  def maxStep(start: Long, stop: Long, bufferSize: Int): Int = {
    if (stop - start + 1 >= bufferSize) {
      bufferSize
    } else {
      (stop - start + 1).toInt
    }
  }
}
