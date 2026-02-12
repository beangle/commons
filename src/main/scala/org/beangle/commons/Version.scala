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

package org.beangle.commons

import java.net.{URI, URL}

/** Library version info. */
object Version {

  /** The library name. */
  def name: String = "Beangle Scala Development Toolkit"

  /** The version from MANIFEST.MF, or SNAPSHOT if not in jar. */
  def version: String = findBundleVersion(Version.getClass)

  /** Reads version from the class's JAR manifest.
   *
   * @param clazz the class (used to locate JAR)
   * @return Bundle-Version, Implementation-Version, or SNAPSHOT/UNKNOWN
   */
  def findBundleVersion(clazz: Class[_]): String = {
    val className = "/" + clazz.getName.replace(".", "/") + ".class"
    val classPath = clazz.getResource(className).toString
    if (classPath.startsWith("jar")) {
      val manifestPath = classPath.replace(className, "/META-INF/MANIFEST.MF")
      val manifest = new java.util.jar.Manifest(URI.create(manifestPath).toURL.openStream)
      val attr = manifest.getMainAttributes
      var version = attr.getValue("Bundle-Version")
      if (null == version) {
        version = attr.getValue("Implementation-Version")
      }
      if (null == version) "UNKNOWN" else version
    } else {
      "SNAPSHOT"
    }
  }
}
