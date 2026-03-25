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

import org.beangle.commons.config.Enviroment
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{Strings, SystemInfo}

import java.io.File
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.{CookieManager, Socket}
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.*

/** HTTPS client factory (default and trust-all). */
object Https {

  /** Creates [[java.net.http.HttpClient]] with standard SSL verification and no cookie jar.
   * `trustAll` follows system property `beangle.https.trust-all`; client is
   * Each call is independent unless you add your own [[java.net.CookieHandler]]. */
  def createDefaultClient(): HttpClient = {
    val trustAll = Enviroment.Default.getProperty("beangle.https.trust-all").map(_.toString).orNull
    createClient(java.lang.Boolean.valueOf(trustAll), false, Duration.ofSeconds(30))
  }

  def defaultUserAgent: String = {
    Enviroment.Default.getProperty("beangle.https.user-agent") match {
      case None =>
        var osName = SystemInfo.os.name
        if (osName.startsWith("Linux")) {
          val release = new File("/etc/os-release")
          if (release.exists()) {
            val lines = IOs.readProperties(release.toURI.toURL)
            osName = Strings.capitalize(lines.getOrElse("ID", osName))
          }
        }
        val os = s"${osName}/${SystemInfo.os.version}"
        s"Java/${SystemInfo.jvm.version} (${os})"
      case Some(ua) => ua.toString
    }
  }

  /** Creates a [[java.net.http.HttpClient]]: strict or trust-all SSL, optionally with session
   * cookies via an in-memory [[java.net.CookieManager]].
   *
   * @param trustAll      if true, [[createTrustAllClient]] (trust all certificates);
   * @param cookieSupport if true, attach a new [[java.net.CookieManager]]
   * @return the HttpClient
   */
  def createClient(trustAll: Boolean, cookieSupport: Boolean, timeout: Duration): HttpClient = {
    if (trustAll) {
      createTrustAllClient(cookieSupport, timeout)
    } else {
      val b = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(Redirect.NORMAL)
      if (cookieSupport) {
        b.cookieHandler(new CookieManager())
      }
      b.build()
    }
  }

  /** Creates [[java.net.http.HttpClient]] that trusts all certificates (for dev/testing only).
   *
   * @param cookieSupport if true, attach a new [[java.net.CookieManager]]
   */
  private def createTrustAllClient(cookieSupport: Boolean, timeout: Duration): HttpClient = {
    try {
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, Array(NullTrustManager), new java.security.SecureRandom())
      val b = HttpClient.newBuilder.sslContext(sslContext)
        .sslParameters(createWideOpenSslParams())
        .connectTimeout(timeout)
        .followRedirects(Redirect.NORMAL)
      if (cookieSupport) {
        b.cookieHandler(new CookieManager())
      }
      b.build()
    } catch {
      case e: Exception =>
        throw new RuntimeException("Failed to create SSL-ignoring HttpClient", e)
    }
  }

  // wide SSL params: no hostname verification, support common TLS protocols
  private def createWideOpenSslParams() = {
    val sslParams = new SSLParameters
    // disable hostname vs certificate domain check (null disables)
    sslParams.setEndpointIdentificationAlgorithm(null)
    // support common TLS protocols
    sslParams.setProtocols(Array[String]("TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"))
    // support all default cipher suites
    sslParams.setCipherSuites(SSLContext.getDefault.getDefaultSSLParameters.getCipherSuites)
    sslParams
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

    override def getAcceptedIssuers: Array[X509Certificate] = Array.empty[X509Certificate]
  }

}
