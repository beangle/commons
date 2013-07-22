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
package org.beangle.commons.web.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
//remove if not needed
import scala.collection.JavaConversions._

object HttpUtils {

  def getResponseText(urlString: String): String = {
    var url = new URL(urlString)
    val uri = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath, url.getQuery,
      url.getRef)
    url = uri.toURL()
    getResponseText(url, null)
  }

  def getResponseText(constructedUrl: URL, encoding: String): String = {
    getResponseText(constructedUrl, null, encoding)
  }

  def getResponseText(constructedUrl: URL, hostnameVerifier: HostnameVerifier, encoding: String): String = {
    var conn: HttpURLConnection = null
    try {
      conn = constructedUrl.openConnection().asInstanceOf[HttpURLConnection]
      if (conn.isInstanceOf[HttpsURLConnection] && null != hostnameVerifier) {
        conn.asInstanceOf[HttpsURLConnection].setHostnameVerifier(hostnameVerifier)
      }
      var in: BufferedReader = null
      in = if (null == encoding) new BufferedReader(new InputStreamReader(conn.getInputStream)) else new BufferedReader(new InputStreamReader(conn.getInputStream,
        encoding))
      var line: String = in.readLine()
      val stringBuffer = new StringBuffer(255)
      stringBuffer.synchronized {
        while (line != null) {
          stringBuffer.append(line)
          stringBuffer.append("\n")
          line = in.readLine()
        }
        stringBuffer.toString
      }
    } catch {
      case e: Exception => throw new RuntimeException(e)
    } finally {
      if (conn != null) {
        conn.disconnect()
      }
    }
  }
}
