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

/** System information
 *
 * @author chaostone
 * @since 3.0.2
 */
object SystemInfo {

  def properties: collection.Map[String, String] = {
    import scala.jdk.javaapi.CollectionConverters.asScala
    asScala(System.getProperties)
  }

  def env: collection.Map[String, String] = {
    import scala.jdk.javaapi.CollectionConverters.asScala
    asScala(System.getenv())
  }

  def os: Os = new Os(System.getProperties)

  def user: User = new User(System.getProperties)

  def lang: Lang = new Lang(System.getProperties)

  def jvm: Jvm = new Jvm(System.getProperties)

  def jre: JavaRuntime = new JavaRuntime(System.getProperties)

  def tmpDir: String = System.getProperty("java.io.tmpdir")

  class User(props: ju.Properties) {

    def name: String = props.getProperty("user.name")

    def home: String = props.getProperty("user.home")

    /** work dir */
    def dir: String = props.getProperty("user.dir")

    def language: String = props.getProperty("user.language")

    def country: String = {
      props.getProperty("user.country", props.get("user.region").asInstanceOf[String])
    }
  }

  class JavaRuntime(props: ju.Properties) {

    def name: String = props.getProperty("java.runtime.name")

    def version: String = props.getProperty("java.runtime.version")

    def home: String = props.getProperty("java.home")

    def classpath: String = props.getProperty("java.class.path")

    def classVersion: String = props.getProperty("java.class.version")

    def libraryPath: String = props.getProperty("java.library.path")

    def tmpDir: String = props.getProperty("java.io.tmpdir")

    def fileEncoding: String = props.getProperty("file.encoding")
  }

  class Jvm(props: ju.Properties) {

    def name: String = props.getProperty("java.vm.name")

    def version: String = props.getProperty("java.vm.version")

    def vendor: String = props.getProperty("java.vm.vendor")

    def info: String = props.getProperty("java.vm.info")

    def specName: String = props.getProperty("java.vm.specification.name")

    def specVersion: String = props.getProperty("java.vm.specification.version")

    def spacVendor: String = props.getProperty("java.vm.specification.vendor")
  }

  class Lang(props: ju.Properties) {

    def version: String = props.getProperty("java.version")

    def vendor: String = props.getProperty("java.vendor")

    def vendorUrl: String = props.getProperty("java.vendor.url")

    def specName: String = props.getProperty("java.specification.name")

    def specVersion: String = props.getProperty("java.specification.version")

    def specVendor: String = props.getProperty("java.specification.vendor")
  }

  class Os(props: ju.Properties) {

    def name: String = props.getProperty("os.name")

    def version: String = props.getProperty("os.version")

    def arch: String = props.getProperty("os.arch")

    def fileSeparator: String = props.getProperty("file.separator")

    def lineSeparator: String = props.getProperty("line.separator")

    def pathSeparator: String = props.getProperty("path.separator")
  }
}
