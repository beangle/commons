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

import java.net.Socket
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.*

object Https {

  private val Timeout = Duration.ofSeconds(10)

  def createDefaultClient(): HttpClient = {
    HttpClient.newBuilder().connectTimeout(Timeout).followRedirects(Redirect.NORMAL).build()
  }

  def createTrustAllClient(): HttpClient = {
    try {
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, Array(NullTrustManager), new java.security.SecureRandom())
      HttpClient.newBuilder.sslContext(sslContext)
        .sslParameters(createWideOpenSslParams())
        .connectTimeout(Timeout)
        .followRedirects(Redirect.NORMAL)
        .build()
    } catch {
      case e: Exception =>
        throw new RuntimeException("创建忽略SSL校验的HttpClient失败", e)
    }
  }

  // 创建宽泛的SSLParameters：关闭主机名校验 + 支持所有常见SSL/TLS协议
  private def createWideOpenSslParams() = {
    val sslParams = new SSLParameters
    //关闭主机名与证书域名的匹配校验（设为null即可）
    sslParams.setEndpointIdentificationAlgorithm(null)
    //支持所有常见SSL/TLS协议（包括部分旧协议，极致宽泛）
    sslParams.setProtocols(Array[String]("TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"))
    // 可选：支持所有加密套件（进一步放宽）
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
