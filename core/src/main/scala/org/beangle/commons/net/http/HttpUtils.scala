/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.net.http

import java.io.{ BufferedReader, ByteArrayOutputStream, InputStreamReader }
import java.net.{ HttpURLConnection, URL }

import org.beangle.commons.io.IOs
import org.beangle.commons.logging.Logging

import javax.net.ssl.{ HostnameVerifier, HttpsURLConnection }

object HttpUtils extends Logging {

  def getData(urlString: String): Option[Array[Byte]] = {
    val url = new URL(urlString)
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(5 * 1000)
      conn.setReadTimeout(5 * 1000)
      conn.setRequestMethod(HttpMethods.GET)
      conn.setDoOutput(true)
      if (conn.getResponseCode == 200) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Some(bos.toByteArray)
      } else {
        None
      }
    } catch {
      case e: Exception => logger.error("Cannot open url " + urlString + ",for " + e.getMessage); None
    } finally {
      if (null != conn) conn.disconnect()
    }
  }

  def getText(urlString: String): Option[String] = {
    getText(new URL(urlString), null)
  }

  def getText(constructedUrl: URL, encoding: String): Option[String] = {
    getText(constructedUrl, null, encoding)
  }

  def getText(url: URL, hostnameVerifier: HostnameVerifier, encoding: String): Option[String] = {
    var conn: HttpURLConnection = null
    var in: BufferedReader = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(5 * 1000)
      conn.setReadTimeout(5 * 1000)
      conn.setRequestMethod(HttpMethods.GET)
      conn.setDoOutput(true)

      if (conn.isInstanceOf[HttpsURLConnection] && null != hostnameVerifier) {
        conn.asInstanceOf[HttpsURLConnection].setHostnameVerifier(hostnameVerifier)
      }
      if (conn.getResponseCode == 200) {
        in =
          if (null == encoding) new BufferedReader(new InputStreamReader(conn.getInputStream))
          else new BufferedReader(new InputStreamReader(conn.getInputStream, encoding))
        var line: String = in.readLine()
        val sb = new StringBuilder(255)
        while (line != null) {
          sb.append(line)
          sb.append("\n")
          line = in.readLine()
        }
        Some(sb.toString)
      } else {
        None
      }
    } catch {
      case e: Exception => logger.error("Cannot open url " + url + " for " + e.getMessage); None
    } finally {
      if (null != in) in.close()
      if (null != conn) conn.disconnect()
    }
  }
}
