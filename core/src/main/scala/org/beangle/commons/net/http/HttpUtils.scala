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

import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Charsets
import org.beangle.commons.logging.Logging

import java.io.{BufferedReader, ByteArrayOutputStream, InputStreamReader, OutputStreamWriter}
import java.net.HttpURLConnection.*
import java.net.{HttpURLConnection, URL, URLConnection}
import java.nio.charset.Charset

object HttpUtils extends Logging {

  private val Timeout = 10 * 1000

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  def toString(httpCode: Int): String = statusMap.getOrElse(httpCode, String.valueOf(httpCode))

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
    requestBy(conn, method)
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
    getData(new URL(urlString), method, None)
  }

  def getData(url: URL, method: String, username: String, password: String): Response = {
    getData(url, method, Some({ x =>
      x.addRequestProperty("Authorization", "Basic " + Base64.encode(s"$username:$password".getBytes))
    }))
  }

  def getData(url: URL, method: String, f: Option[(HttpURLConnection) => Unit]): Response = {
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      requestBy(conn, method)
      conn.setUseCaches(false)
      conn.setDoOutput(false)
      Https.noverify(conn)
      f foreach (x => x(conn))

      if (conn.getResponseCode == HTTP_OK) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Response(conn.getResponseCode, bos.toByteArray)
      } else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(conn, url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  def getText(urlString: String): Response = {
    getText(new URL(urlString), HttpMethods.GET, Charsets.UTF_8, None)
  }

  def getText(url: URL, method: String, encoding: Charset): Response = {
    getText(url, method, encoding, None)
  }

  def getText(url: URL, method: String, encoding: Charset, username: String, password: String): Response = {
    getText(url, method, encoding, Some({ x =>
      x.addRequestProperty("Authorization", "Basic " + Base64.encode(s"$username:$password".getBytes))
    }))
  }

  def getText(url: URL, method: String, encoding: Charset, f: Option[(URLConnection) => Unit]): Response = {
    var conn: HttpURLConnection = null
    var in: BufferedReader = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      requestBy(conn, method)
      conn.setDoOutput(false)
      conn.setUseCaches(false)
      Https.noverify(conn)
      f foreach (x => x(conn))
      if conn.getResponseCode == HTTP_OK then
        in = new BufferedReader(new InputStreamReader(conn.getInputStream, encoding))
        var line: String = in.readLine()
        val sb = new StringBuilder(255)
        while (line != null) {
          sb.append(line)
          sb.append("\n")
          line = in.readLine()
        }
        Response(HTTP_OK, sb.toString)
      else
        Response(conn.getResponseCode, conn.getResponseMessage)
    } catch {
      case e: Exception => error(conn, url, e)
    } finally {
      if (null != in) in.close()
      if (null != conn) conn.disconnect()
    }
  }

  def invoke(url: URL, body: AnyRef, contentType: String): Response = {
    invoke(url, body, contentType, None)
  }

  def invoke(url: URL, body: AnyRef, contentType: String, username: String, password: String): Response = {
    invoke(url, body, contentType, Some({ x =>
      x.addRequestProperty("Authorization", "Basic " + Base64.encode(s"$username:$password".getBytes))
    }))
  }

  def invoke(url: URL, body: AnyRef, contentType: String, f: Option[(URLConnection) => Unit]): Response = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    Https.noverify(conn)
    conn.setDoOutput(true)
    requestBy(conn, HttpMethods.POST)
    conn.setRequestProperty("Content-Type", contentType)
    f foreach (x => x(conn))
    val os = conn.getOutputStream
    body match {
      case ba: Array[Byte] => os.write(ba)
      case _ =>
        val osw = new OutputStreamWriter(os, "UTF-8")
        osw.write(body.toString)
        osw.flush()
        osw.close()
    }
    os.close() //don't forget to close the OutputStream
    try {
      conn.connect()
      val lines = IOs.readString(conn.getInputStream)
      Response(conn.getResponseCode, lines)
    } catch {
      case e: Exception => error(conn, url, e)
    } finally
      if (null != conn) conn.disconnect()
  }

  private[this] def error(conn: HttpURLConnection, url: URL, e: Exception): Response = {
    logger.info("Cannot open url " + url + " " + e.getMessage)
    Response(conn.getResponseCode, e.getMessage)
  }

  private def requestBy(conn: HttpURLConnection, method: String): Unit = {
    conn.setConnectTimeout(Timeout)
    conn.setReadTimeout(Timeout)
    conn.setRequestMethod(method)
  }
}
