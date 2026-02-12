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

package org.beangle.commons.lang

import java.util as ju

/** System information utilities.
 *
 * @author chaostone
 * @since 3.0.2
 */
object SystemInfo {

  /** System properties as a map. */
  def properties: collection.Map[String, String] = {
    import scala.jdk.javaapi.CollectionConverters.asScala
    asScala(System.getProperties)
  }

  /** Environment variables as a map. */
  def env: collection.Map[String, String] = {
    import scala.jdk.javaapi.CollectionConverters.asScala
    asScala(System.getenv())
  }

  /** OS information. */
  def os: Os = new Os(System.getProperties)

  /** User information. */
  def user: User = new User(System.getProperties)

  /** Language/locale information. */
  def lang: Lang = new Lang(System.getProperties)

  /** JVM information. */
  def jvm: Jvm = new Jvm(System.getProperties)

  /** Java runtime information. */
  def jre: JavaRuntime = new JavaRuntime(System.getProperties)

  /** Temporary directory path. */
  def tmpDir: String = System.getProperty("java.io.tmpdir")

  /** User-related system properties. */
  class User(props: ju.Properties) {

    /** User account name. */
    def name: String = props.getProperty("user.name")

    /** User home directory. */
    def home: String = props.getProperty("user.home")

    /** Working directory. */
    def dir: String = props.getProperty("user.dir")

    /** User language. */
    def language: String = props.getProperty("user.language")

    /** User country/region. */
    def country: String = {
      props.getProperty("user.country", props.get("user.region").asInstanceOf[String])
    }
  }

  /** Java runtime properties. */
  class JavaRuntime(props: ju.Properties) {

    /** Runtime name. */
    def name: String = props.getProperty("java.runtime.name")

    /** Runtime version. */
    def version: String = props.getProperty("java.runtime.version")

    /** Java home directory. */
    def home: String = props.getProperty("java.home")

    /** Class path. */
    def classpath: String = props.getProperty("java.class.path")

    /** Class file version. */
    def classVersion: String = props.getProperty("java.class.version")

    /** Library path. */
    def libraryPath: String = props.getProperty("java.library.path")

    /** Temp directory. */
    def tmpDir: String = props.getProperty("java.io.tmpdir")

    /** File encoding. */
    def fileEncoding: String = props.getProperty("file.encoding")
  }

  /** JVM properties. */
  class Jvm(props: ju.Properties) {

    /** VM name. */
    def name: String = props.getProperty("java.vm.name")

    /** VM version. */
    def version: String = props.getProperty("java.vm.version")

    /** VM vendor. */
    def vendor: String = props.getProperty("java.vm.vendor")

    /** VM info. */
    def info: String = props.getProperty("java.vm.info")

    /** VM spec name. */
    def specName: String = props.getProperty("java.vm.specification.name")

    /** VM spec version. */
    def specVersion: String = props.getProperty("java.vm.specification.version")

    /** VM spec vendor. */
    def spacVendor: String = props.getProperty("java.vm.specification.vendor")
  }

  /** Java language properties. */
  class Lang(props: ju.Properties) {

    /** Java version. */
    def version: String = props.getProperty("java.version")

    /** Java vendor. */
    def vendor: String = props.getProperty("java.vendor")

    /** Vendor URL. */
    def vendorUrl: String = props.getProperty("java.vendor.url")

    /** Spec name. */
    def specName: String = props.getProperty("java.specification.name")

    /** Spec version. */
    def specVersion: String = props.getProperty("java.specification.version")

    /** Spec vendor. */
    def specVendor: String = props.getProperty("java.specification.vendor")
  }

  /** OS properties. */
  class Os(props: ju.Properties) {

    /** OS name. */
    def name: String = props.getProperty("os.name")

    /** OS version. */
    def version: String = props.getProperty("os.version")

    /** OS architecture. */
    def arch: String = props.getProperty("os.arch")

    /** File separator. */
    def fileSeparator: String = props.getProperty("file.separator")

    /** Line separator. */
    def lineSeparator: String = props.getProperty("line.separator")

    /** Path separator. */
    def pathSeparator: String = props.getProperty("path.separator")
  }
}
