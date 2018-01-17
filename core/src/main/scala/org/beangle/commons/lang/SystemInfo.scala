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
package org.beangle.commons.lang

import java.net.{ InetAddress, NetworkInterface, UnknownHostException }

import scala.collection.mutable
/**
 * System information
 *
 * @author chaostone
 * @since 3.0.2
 */
object SystemInfo {

  private def sysProperties(): Map[String, String] = {
    val origin = new mutable.HashMap[String, String]
    val props = System.getProperties
    val enumer = props.propertyNames
    while (enumer.hasMoreElements()) {
      val name = enumer.nextElement().asInstanceOf[String]
      origin.put(name, props.getProperty(name))
    }
    origin.toMap
  }

  val properties = sysProperties

  val os = new Os(properties)

  val user = new User(properties)

  val java = new Java(properties)

  val jvm = new Jvm(properties)

  val javaSpec = new JavaSpec(properties)

  val jvmSpec = new JvmSpec(properties)

  val jre = new JavaRuntime(properties)

  def tmpDir: String = System.getProperty("java.io.tmpdir")

  def host = new Host()

  class Host {

    def hostname: String = {
      try {
        InetAddress.getLocalHost.getHostName
      } catch {
        case e: UnknownHostException => "unknownhost"
      }
    }

    def addresses: Map[String, List[String]] = {
      val addresses = new mutable.HashMap[String, List[String]]

      try {
        val e = NetworkInterface.getNetworkInterfaces
        while (e.hasMoreElements) {
          val networkInterface = e.nextElement()
          val name = networkInterface.getDisplayName
          val e2 = networkInterface.getInetAddresses
          while (e2.hasMoreElements()) {
            addresses += (name -> (e2.nextElement().getHostAddress :: addresses.getOrElse(name, Nil)))
          }
        }
      } catch {
        case e: Exception =>
      }
      addresses.toMap
    }
  }

  class User(properties: Map[String, String]) {

    val name = properties("user.name")

    val home = properties("user.home")

    val dir = properties("user.dir")

    val language = properties("user.language")

    val country = properties.get("user.country") match {
      case Some(c) => c
      case _ => properties.get("user.region").orNull
    }
  }

  class JavaRuntime(properties: Map[String, String]) {

    val name = properties("java.runtime.name")

    val version = properties("java.runtime.version")

    val home = properties("java.home")

    val extDirs = properties("java.ext.dirs")

    val endorsedDirs = properties("java.endorsed.dirs")

    val classpath = properties("java.class.path")

    val classVersion = properties("java.class.version")

    val libraryPath = properties("java.library.path")

    val tmpDir = properties("java.io.tmpdir")

    val fileEncoding = properties("file.encoding")
  }

  class JvmSpec(properties: Map[String, String]) {

    val name = properties("java.vm.specification.name")

    val version = properties("java.vm.specification.version")

    val vendor = properties("java.vm.specification.vendor")
  }

  class JavaSpec(properties: Map[String, String]) {

    val name = properties("java.specification.name")

    val version = properties("java.specification.version")

    val vendor = properties("java.specification.vendor")
  }

  class Jvm(properties: Map[String, String]) {

    val name = properties("java.vm.name")

    val version = properties("java.vm.version")

    val vendor = properties("java.vm.vendor")

    val info = properties("java.vm.info")
  }

  class Java(properties: Map[String, String]) {

    val version = properties("java.version")

    val vendor = properties("java.vendor")

    val vendorUrl = properties("java.vendor.url")

  }

  class Os(properties: Map[String, String]) {

    val name = properties("os.name")

    val version = properties("os.version")

    val arch = properties("os.arch")

    val fileSeparator = properties("file.separator")

    val lineSeparator = properties("line.separator")

    val pathSeparator = properties("path.separator")
  }
}
