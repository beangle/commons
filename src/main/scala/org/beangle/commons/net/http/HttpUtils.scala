/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.net.http

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Charsets

import java.io.*
import java.net.*
import java.net.HttpURLConnection.*
import java.nio.charset.Charset

object HttpUtils {

  private val Timeout = 10 * 1000

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  def toString(httpCode: Int): String = {
    statusMap.getOrElse(httpCode, String.valueOf(httpCode))
  }

  def isAlive(url: String): Boolean = {
    access(url).isOk
  }

  def access(url: String): ResourceStatus = {
    access(URI.create(url).toURL)
  }

  def access(url: URL): ResourceStatus = {
    try {
      val hc = followRedirect(url.openConnection(), HttpMethods.HEAD)
      val rc = hc.getResponseCode
      rc match {
        case HTTP_OK =>
          val supportRange = "bytes" == hc.getHeaderField("Accept-Ranges")
          ResourceStatus(rc, hc.getURL, hc.getHeaderFieldLong("Content-Length", 0), hc.getLastModified, supportRange)
        case _ => ResourceStatus(rc, hc.getURL, -1, -1, supportRange = false)
      }
    } catch {
      case _: Exception => ResourceStatus(HTTP_NOT_FOUND, url, -1, -1, false)
    }
  }

  @scala.annotation.tailrec
  def followRedirect(c: URLConnection, method: String): HttpURLConnection = {
    val conn = c.asInstanceOf[HttpURLConnection]
    conn.setInstanceFollowRedirects(false)
    setup(conn, method)
    val rc = conn.getResponseCode
    rc match {
      case HTTP_OK => conn
      case HTTP_MOVED_TEMP | HTTP_MOVED_PERM =>
        val newLoc = conn.getHeaderField("location")
        followRedirect(URI.create(newLoc).toURL.openConnection, method)
      case _ => conn
    }
  }

  def getData(urlString: String): Response = {
    getData(URI.create(urlString).toURL, None)
  }

  def getData(url: URL, f: Option[HttpURLConnection => Unit]): Response = {
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      conn.setRequestMethod(HttpMethods.GET)
      f foreach (x => x(conn))
      conn = followRedirect(conn, HttpMethods.GET)
      if (conn.getResponseCode == HTTP_OK) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Response(conn.getResponseCode, bos.toByteArray)
      } else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def getText(urlString: String): Response = {
    getText(URI.create(urlString).toURL, Charsets.UTF_8, None)
  }

  def getText(url: URL, encoding: Charset): Response = {
    getText(url, encoding, None)
  }

  def getText(url: URL, encoding: Charset, f: Option[URLConnection => Unit]): Response = {
    var conn: HttpURLConnection = null
    var in: BufferedReader = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      f foreach (x => x(conn))
      conn = followRedirect(conn, HttpMethods.GET)
      if conn.getResponseCode == HTTP_OK then
        in = new BufferedReader(new InputStreamReader(conn.getInputStream, encoding))
        var line: String = in.readLine()
        val sb = new StringBuilder(255)
        while (line != null) {
          sb.append(line)
          line = in.readLine()
          if (null != line) sb.append("\n")
        }
        Response(HTTP_OK, sb.toString)
      else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(url, e)
    } finally {
      if (null != in) in.close()
      if (null != conn) conn.disconnect()
    }
  }

  def post(url: URL, body: AnyRef, contentType: String): Response = {
    invoke(url, HttpMethods.POST, Request.build(body, contentType), None)
  }

  def post(url: URL, request: Request): Response = {
    invoke(url, HttpMethods.POST, request, None)
  }

  def put(url: URL, request: Request): Response = {
    invoke(url, HttpMethods.PUT, request, None)
  }

  def delete(url: URL, request: Request): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    setup(conn, HttpMethods.DELETE)
    writeHeaders(conn, request)
    try {
      conn.connect()
      val bos = new ByteArrayOutputStream
      IOs.copy(conn.getInputStream, bos)
      Response(conn.getResponseCode, bos.toByteArray)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def invoke(url: URL, method: String, request: Request, f: Option[URLConnection => Unit] = None): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setDoOutput(true)
    require(HttpMethods.POST == method || HttpMethods.PUT == method, "Only support POST or PUT in invoke")
    setup(conn, method)
    writeHeaders(conn, request)
    f foreach (x => x(conn))
    val os = conn.getOutputStream
    request.body match {
      case ba: Array[Byte] => os.write(ba)
      case is: InputStream => IOs.copy(is, os)
      case v => os.write(v.toString.getBytes(Charsets.UTF_8))
    }
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      val bos = new ByteArrayOutputStream
      IOs.copy(conn.getInputStream, bos)
      Response(conn.getResponseCode, bos.toByteArray)
    } catch {
      case e: Exception => error(url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def download(c: URLConnection, location: File): Boolean = {
    val conn = followRedirect(c, "GET")
    location.getParentFile.mkdirs()
    var input: InputStream = null
    var output: OutputStream = null
    try {
      val file = new File(location.toString + ".part")
      file.delete()
      val buffer = Array.ofDim[Byte](1024 * 4)
      input = conn.getInputStream
      output = new FileOutputStream(file)
      var n = input.read(buffer)
      while (-1 != n) {
        output.write(buffer, 0, n)
        n = input.read(buffer)
      }
      //先关闭文件读写，再改名
      IOs.close(input, output)
      input = null
      output = null
      if (location.exists()) location.delete()
      file.renameTo(location)
      true
    } catch {
      case e: Throwable => false
    }
    finally {
      IOs.close(input, output)
    }
  }

  private[this] def error(url: URL, e: Exception): Response = {
    Response(404, e.getMessage)
  }

  private def setup(conn: HttpURLConnection, method: String): Unit = {
    conn.setConnectTimeout(Timeout)
    conn.setReadTimeout(Timeout)
    conn.setRequestMethod(method)
    Https.noverify(conn)
  }

  private def writeHeaders(conn: HttpURLConnection, request: Request): Unit = {
    request.headers foreach { (k, v) => conn.addRequestProperty(k, v.toString) }
    request.authorization foreach { auth => conn.setRequestProperty("Authorization", auth) }
    conn.setRequestProperty("Content-Type", request.contentType)
  }
}
