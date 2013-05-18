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
package org.beangle.commons.lang

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.Enumeration
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import scala.reflect.{ BeanProperty, BooleanBeanProperty }
import scala.collection.JavaConversions._
import java.util.Properties
/**
 * System information
 *
 * @author chaostone
 * @since 3.0.2
 */
object SystemInfo {

  private var os: Os = _

  private var user: User = _

  private var java: Java = _

  private var jvm: Jvm = _

  private var javaSpec: JavaSpec = _

  private var jvmSpec: JvmSpec = _

  private var javaRuntime: JavaRuntime = _

  private val usedProperties = CollectUtils.newHashSet[String]

  private def init() {
    val origin = new HashMap[String, String]
    val enumer = System.getProperties().propertyNames
    while (enumer.hasMoreElements()) {
      val name = enumer.nextElement().asInstanceOf[String]
      origin.put(name, System.getProperties().getProperty(name))
    }
    val properties = CollectUtils.newHashMap[String, String](origin)
    os = new Os(properties)
    user = new User(properties)
    java = new Java(properties)
    jvm = new Jvm(properties)
    javaSpec = new JavaSpec(properties)
    jvmSpec = new JvmSpec(properties)
    javaRuntime = new JavaRuntime(properties)
    origin.keySet().removeAll(properties.keySet())
    usedProperties.addAll(origin.keySet())
  }

  init()

  def getOs = os
  def getUser = user
  def getJava = java
  def getJvm = jvm
  def getJavaSpec = javaSpec
  def getJvmSpec = jvmSpec
  def getJavaRuntime = javaRuntime

  def getTmpDir(): String = System.getProperty("java.io.tmpdir")

  def getUsedproperties(): Set[String] = usedProperties

  def getHost(): Host = new Host()

  class Host() {

    var hostname: String = _

    val addresses = CollectUtils.newHashMap[String, List[String]]

    try {
      val localhost = InetAddress.getLocalHost
      hostname = localhost.getHostName
    } catch {
      case e: UnknownHostException => hostname = "localhost"
    }

    try {
      var e = NetworkInterface.getNetworkInterfaces
      while (e.hasMoreElements()) {
        val networkInterface = e.nextElement()
        val name = networkInterface.getDisplayName
        val addrs = CollectUtils.newArrayList[String]
        var e2 = networkInterface.getInetAddresses
        while (e2.hasMoreElements()) addrs.add(e2.nextElement().getHostAddress)
        addresses.put(name, addrs)
      }
    } catch {
      case e: Exception =>
    }

    def getHostname(): String = hostname

    def getAddresses(): Map[String, List[String]] = addresses
  }

  class User(properties: Map[String, String]) {

    val name = properties.remove("user.name")

    val home = properties.remove("user.home")

    val language = properties.remove("user.language")

    var country = properties.remove("user.country")

    if (null == country) country = properties.remove("user.region")

    def getName(): String = name

    def getHome(): String = home

    def getLanguage(): String = language

    def getCountry(): String = country
  }

  class JavaRuntime(properties: Map[String, String]) {

    val name = properties.remove("java.runtime.name")

    val version = properties.remove("java.runtime.version")

    val home = properties.remove("java.home")

    val extDirs = properties.remove("java.ext.dirs")

    val endorsedDirs = properties.remove("java.endorsed.dirs")

    val classpath = properties.remove("java.class.path")

    val classVersion = properties.remove("java.class.version")

    val libraryPath = properties.remove("java.library.path")

    val tmpDir = properties.remove("java.io.tmpdir")

    val fileEncoding = properties.remove("file.encoding")

    def getName(): String = name

    def getVersion(): String = version

    def getHome(): String = home

    def getExtDirs(): String = extDirs

    def getEndorsedDirs(): String = endorsedDirs

    def getClasspath(): String = classpath

    def getClassVersion(): String = classVersion

    def getLibraryPath(): String = libraryPath

    def getTmpDir(): String = tmpDir

    def getFileEncoding(): String = fileEncoding
  }

  class JvmSpec(properties: Map[String, String]) {

    val name = properties.remove("java.vm.specification.name")

    val version = properties.remove("java.vm.specification.version")

    val vendor = properties.remove("java.vm.specification.vendor")

    def getName(): String = name

    def getVersion(): String = version

    def getVendor(): String = vendor
  }

  class JavaSpec(properties: Map[String, String]) {

    val name = properties.remove("java.specification.name")

    val version = properties.remove("java.specification.version")

    val vendor = properties.remove("java.specification.vendor")

    def getName(): String = name

    def getVersion(): String = version

    def getVendor(): String = vendor
  }

  class Jvm(properties: Map[String, String]) {

    val name = properties.remove("java.vm.name")

    val version = properties.remove("java.vm.version")

    val vendor = properties.remove("java.vm.vendor")

    val info = properties.remove("java.vm.info")

    def getName(): String = name

    def getVersion(): String = version

    def getVendor(): String = vendor

    def getInfo(): String = info
  }

  class Java(properties: Map[String, String]) {

    val version = properties.remove("java.version")

    val vendor = properties.remove("java.vendor")

    val vendorUrl = properties.remove("java.vendor.url")

    def getVersion(): String = version

    def getVendor(): String = vendor

    def getVendorUrl(): String = vendorUrl
  }

  class Os(properties: Map[String, String]) {

    val name = properties.remove("os.name")

    val version = properties.remove("os.version")

    val arch = properties.remove("os.arch")

    val fileSeparator = properties.remove("file.separator")

    val lineSeparator = properties.remove("line.separator")

    val pathSeparator = properties.remove("path.separator")

    def getName(): String = name

    def getVersion(): String = version

    def getArch(): String = arch

    def getFileSeparator(): String = fileSeparator

    def getLineSeparator(): String = lineSeparator

    def getPathSeparator(): String = pathSeparator
  }
}
