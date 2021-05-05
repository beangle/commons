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
package org.beangle.commons.net.http

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Charsets
import org.beangle.commons.logging.Logging

import java.io.{BufferedReader, ByteArrayOutputStream, InputStreamReader, OutputStreamWriter}
import java.net.HttpURLConnection._
import java.net.{HttpURLConnection, URL, URLConnection}
import java.nio.charset.Charset

object HttpUtils extends Logging {

  private val Timeout = 15 * 1000

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  def toString(httpCode: Int): String = {
    statusMap.getOrElse(httpCode, String.valueOf(httpCode))
  }

  def access(url: URL): ResourceStatus = {
    val hc = followRedirect(url.openConnection(), "HEAD")
    val rc = hc.getResponseCode
    rc match {
      case HTTP_OK =>
        val supportRange = "bytes" == hc.getHeaderField("Accept-Ranges")
        ResourceStatus(rc, hc.getURL, hc.getHeaderFieldLong("Content-Length", 0), hc.getLastModified, supportRange)
      case _ => ResourceStatus(rc, hc.getURL, -1, -1, supportRange = false)
    }
  }

  @scala.annotation.tailrec
  def followRedirect(c: URLConnection, method: String): HttpURLConnection = {
    val conn = c.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod(method)
    conn.setInstanceFollowRedirects(false)
    Https.noverify(conn)
    val rc = conn.getResponseCode
    rc match {
      case HTTP_OK => conn
      case HTTP_MOVED_TEMP | HTTP_MOVED_PERM =>
        val newLoc = conn.getHeaderField("location")
        followRedirect(new URL(newLoc).openConnection, method)
      case _ => conn
    }
  }

  def getData(urlString: String, method: String = HttpMethods.GET): Response = {
    val url = new URL(urlString)
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(Timeout)
      conn.setReadTimeout(Timeout)
      conn.setRequestMethod(method)
      conn.setUseCaches(false)
      conn.setDoOutput(false)
      Https.noverify(conn)

      if (conn.getResponseCode == HTTP_OK) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Response(conn.getResponseCode, bos.toByteArray)
      } else {
        Response(conn.getResponseCode, conn.getResponseMessage)
      }
    } catch {
      case e: Exception =>
        report(url, e)
        Response(HTTP_NOT_FOUND, e.getMessage)
    } finally {
      if (null != conn) conn.disconnect()
    }
  }

  def getText(urlString: String): Response = {
    getText(new URL(urlString), HttpMethods.GET, Charsets.UTF_8)
  }

  def getText(url: URL, method: String, encoding: Charset): Response = {
    var conn: HttpURLConnection = null
    var in: BufferedReader = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(Timeout)
      conn.setReadTimeout(Timeout)
      conn.setRequestMethod(method)
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      Https.noverify(conn)
      if (conn.getResponseCode == HTTP_OK) {
        in = new BufferedReader(new InputStreamReader(conn.getInputStream, encoding))
        var line: String = in.readLine()
        val sb = new StringBuilder(255)
        while (line != null) {
          sb.append(line)
          sb.append("\n")
          line = in.readLine()
        }
        Response(HTTP_OK, sb.toString)
      } else {
        Response(conn.getResponseCode, conn.getResponseMessage)
      }
    } catch {
      case e: Exception =>
        report(url, e)
        Response(HTTP_NOT_FOUND, e.getMessage)
    } finally {
      if (null != in) in.close()
      if (null != conn) conn.disconnect()
    }
  }

  def invoke(url: URL, body: String, contentType: String): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    conn.setRequestMethod(HttpMethods.POST)
    conn.setRequestProperty("Content-Type", contentType)
    val os = conn.getOutputStream
    val osw = new OutputStreamWriter(os, "UTF-8")
    osw.write(body)
    osw.flush()
    osw.close()
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      //read the inputstream and print it
      val lines = IOs.readString(conn.getInputStream)
      Response(conn.getResponseCode, lines)
    } catch {
      case e: Exception =>
        report(url, e)
        Response(HTTP_NOT_FOUND, conn.getResponseMessage)
    } finally {
      if (null != conn) conn.disconnect()
    }
  }


  private[this] def report(url: URL, e: Exception): Unit = {
    logger.error("Cannot open url " + url, e)
  }
}
