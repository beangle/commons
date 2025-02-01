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

package org.beangle.commons.net

import org.beangle.commons.collection.Collections

import java.io.IOException
import java.net.*

object Networks {

  def url(l: String): URL = URI.create(l).toURL

  def uri(l: String): URI = URI.create(l)

  def url(context: URL, path: String): URL = {
    //context.toURI.resolve(path).toURL
    //error when protocol is jar:file:/C/users/some/file.txt
    new URL(context, path)
  }

  def openURL(l: String): URLConnection = url(l).openConnection()

  def localIPs: Set[String] = {
    val niEnum = NetworkInterface.getNetworkInterfaces
    val ips = Collections.newBuffer[String]("127.0.0.1")
    while (niEnum.hasMoreElements) {
      val ni = niEnum.nextElement()
      if (ni.isUp && !ni.isLoopback) {
        val ipEnum = ni.getInetAddresses
        while (ipEnum.hasMoreElements) {
          ipEnum.nextElement() match {
            case ip: Inet4Address => ips += ip.getHostAddress
            case _ =>
          }
        }
      }
    }
    ips.toSet
  }

  def tryConnectURL(l: String): Option[URL] = {
    try {
      val rs = url(l)
      val conn = rs.openConnection
      conn.connect()
      Some(rs)
    } catch {
      case _: Throwable => None
    }
  }

  def isPortFree(port: Int): Boolean = {
    try
      new Socket("localhost", port).close()
      false
    catch
      case e: IOException => true
  }
}
