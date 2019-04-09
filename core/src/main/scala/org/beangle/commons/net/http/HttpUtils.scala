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

import java.io.{ BufferedReader, ByteArrayOutputStream, InputStreamReader }
import java.net.{ URL, URLConnection }
import java.net.HttpURLConnection.{ HTTP_FORBIDDEN, HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_NOT_FOUND, HTTP_OK, HTTP_UNAUTHORIZED }
import java.net.HttpURLConnection

import org.beangle.commons.io.IOs
import org.beangle.commons.logging.Logging
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import javax.net.ssl.{ HostnameVerifier, HttpsURLConnection, SSLSession, SSLContext, X509TrustManager }

object HttpUtils extends Logging {

  val TrustAllHosts = new HostnameVerifier() {
    def verify(arg0: String, arg1: SSLSession): Boolean = {
      true
    }
  }

  private object NullTrustManager extends X509TrustManager {
    override def checkClientTrusted(c: Array[X509Certificate], at: String): Unit = {
    }
    override def checkServerTrusted(c: Array[X509Certificate], s: String): Unit = {
    }
    override def getAcceptedIssuers(): Array[X509Certificate] = {
      null
    }
  }

  private val statusMap = Map(
    HTTP_OK -> "OK",
    HTTP_FORBIDDEN -> "Access denied!",
    HTTP_NOT_FOUND -> "Not Found",
    HTTP_UNAUTHORIZED -> "Access denied")

  def toString(httpCode: Int): String = {
    statusMap.get(httpCode).getOrElse(String.valueOf(httpCode))
  }

  def followRedirect(c: URLConnection, method: String): HttpURLConnection = {
    val conn = c.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod(method)
    conn.setInstanceFollowRedirects(false)
    val rc = conn.getResponseCode
    rc match {
      case HTTP_OK => conn
      case HTTP_MOVED_TEMP | HTTP_MOVED_PERM =>
        val newLoc = conn.getHeaderField("location")
        followRedirect(new URL(newLoc).openConnection, method)
      case _ => conn
    }
  }

  def getData(url: String): Option[Array[Byte]] = {
    getData(url, TrustAllHosts)
  }

  def getData(urlString: String, hostnameVerifier: HostnameVerifier): Option[Array[Byte]] = {
    val url = new URL(urlString)
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection().asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(5 * 1000)
      conn.setReadTimeout(5 * 1000)
      conn.setRequestMethod(HttpMethods.GET)
      conn.setDoOutput(true)
      configHttps(conn, hostnameVerifier)

      if (conn.getResponseCode == 200) {
        val bos = new ByteArrayOutputStream
        IOs.copy(conn.getInputStream, bos)
        Some(bos.toByteArray)
      } else {
        None
      }
    } catch {
      case e: Exception => logger.error("Cannot open url " + urlString + ",for " + e.getMessage, e); None
    } finally {
      if (null != conn) conn.disconnect()
    }
  }

  def configHttps(connection: HttpURLConnection, verifier: HostnameVerifier): Unit = {
    connection match {
      case conn: HttpsURLConnection =>
        conn.setHostnameVerifier(if (null == verifier) TrustAllHosts else verifier)
        val sslContext = SSLContext.getInstance("SSL", "SunJSSE");
        sslContext.init(null, Array(NullTrustManager), new java.security.SecureRandom());

        val ssf = sslContext.getSocketFactory()
        conn.setSSLSocketFactory(ssf)
      case _ =>
    }
  }

  def getText(urlString: String): Option[String] = {
    getText(new URL(urlString), null)
  }

  def getText(constructedUrl: URL, encoding: String): Option[String] = {
    getText(constructedUrl, TrustAllHosts, encoding)
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
      configHttps(conn, hostnameVerifier)
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
