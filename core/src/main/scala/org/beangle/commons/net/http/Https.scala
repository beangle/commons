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
import java.net.{HttpURLConnection, Socket}
import java.security.cert.X509Certificate

import javax.net.ssl._

object Https {

  def noverify(connection: HttpURLConnection): Unit = {
    connection match {
      case conn: HttpsURLConnection =>
        conn.setHostnameVerifier(TrustAllHosts)
        val sslContext = SSLContext.getInstance("SSL", "SunJSSE");
        sslContext.init(null, Array(NullTrustManager), new java.security.SecureRandom());
        val ssf = sslContext.getSocketFactory()
        conn.setSSLSocketFactory(ssf)
      case _ =>
    }
  }

  object NullTrustManager extends X509ExtendedTrustManager {
    override def checkClientTrusted(c: Array[X509Certificate], at: String): Unit = {
    }
    override def checkClientTrusted(c: Array[X509Certificate], at: String, engine: SSLEngine): Unit = {
    }
    override def checkClientTrusted(c: Array[X509Certificate], at: String, socket: Socket): Unit = {
    }
    override def checkServerTrusted(c: Array[X509Certificate], s: String): Unit = {
    }
    override def checkServerTrusted(c: Array[X509Certificate], s: String, engine: SSLEngine): Unit = {
    }
    override def checkServerTrusted(c: Array[X509Certificate], s: String, socket: Socket): Unit = {
    }
    override def getAcceptedIssuers(): Array[X509Certificate] = {
      null
    }
  }

  object TrustAllHosts extends HostnameVerifier {
    def verify(arg0: String, arg1: SSLSession): Boolean = {
      true
    }
  }
}
