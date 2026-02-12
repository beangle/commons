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
import scala.annotation.nowarn

/** URL/URI creation and path resolution. */
object Networks {

  /** Creates a URL from the given string.
   *
   * @param l the URL string
   * @return the URL
   */
  def url(l: String): URL = URI.create(l).toURL

  /** Creates a URI from the given string.
   *
   * @param l the URI string
   * @return the URI
   */
  def uri(l: String): URI = URI.create(l)

  /** Resolves path relative to context URL.
   *
   * @param context the base URL
   * @param path    the relative path
   * @return the resolved URL
   */
  @nowarn
  def url(context: URL, path: String): URL = {
    //context.toURI.resolve(path).toURL
    //error when protocol is jar:file:/C/users/some/file.txt
    new URL(context, path)
  }

  /** Opens a connection to the URL.
   *
   * @param l the URL string
   * @return the connection
   */
  def openURL(l: String): URLConnection = url(l).openConnection()

  /** Returns the local hostname. */
  def hostname: String = {
    try
      InetAddress.getLocalHost.getHostName
    catch {
      case e: UnknownHostException => "unknownhost"
    }
  }

  /** Returns all IPv4 addresses of this host. */
  def ipv4: Set[String] = {
    addresses(1)
  }

  /** Returns all IPv6 addresses of this host. */
  def ipv6: Set[String] = {
    addresses(2)
  }

  /** Returns network addresses. family: 0=all, 1=IPv4, 2=IPv6.
   *
   * @param family 0 for all, 1 for IPv4 only, 2 for IPv6 only
   * @return set of address strings
   */
  def addresses(family: Int = 0): Set[String] = {
    val niEnum = NetworkInterface.getNetworkInterfaces
    val ips = Collections.newBuffer[String]
    family match {
      case 1 => ips.addOne("127.0.0.1")
      case 2 => ips.addOne("::1")
      case _ => ips.addOne("127.0.0.1").addOne("::1")
    }
    while (niEnum.hasMoreElements) {
      val ni = niEnum.nextElement()
      if (ni.isUp && !ni.isLoopback) {
        val ipEnum = ni.getInetAddresses
        while (ipEnum.hasMoreElements) {
          val ip = ipEnum.nextElement()
          if (family == 1 && ip.isInstanceOf[Inet4Address] ||
            family == 2 && ip.isInstanceOf[Inet6Address] ||
            family == 0) {
            ips += ip.getHostAddress
          }
        }
      }
    }
    ips.toSet
  }

  /** Tries to connect to the URL; returns None on failure.
   *
   * @param l the URL string
   * @return Some(URL) if connect succeeds, None otherwise
   */
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

  /** Returns true if no process is listening on the port.
   *
   * @param port the port number
   * @return true if free
   */
  def isPortFree(port: Int): Boolean = {
    try
      new Socket("localhost", port).close()
      false
    catch
      case e: IOException => true
  }

  /** Finds the specified number of free ports starting from startPort.
   *
   * @param startPort the port to start scanning from
   * @param portCount the number of free ports to find (1-100)
   * @return sequence of free port numbers
   */
  def nextFreePorts(startPort: Int, portCount: Int): Seq[Int] = {
    require(portCount >= 1 && portCount <= 100, s"${portCount} is to small or too big")
    val ports = Collections.newBuffer[Int]
    var port = startPort
    while (ports.size < portCount) {
      if Networks.isPortFree(port) then ports.addOne(port)
      port += 1
    }
    ports.toSeq
  }
}
