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

package org.beangle.commons.io

import org.beangle.commons.lang.Strings

import java.net.{URI, URL, URLConnection}
import java.util.jar.JarFile

/** JAR URL and file utilities. */
object Jars {

  /** JAR URL protocols. */
  val protocols = Set("jar", "zip", "wsjar", "vsfzip")

  /** Separator between JAR URL and entry path. */
  val URLSeparator = "!/"

  /** Returns true if the URL is a JAR/zip URL.
   *
   * @param url the URL to check
   * @return true if JAR protocol
   */
  def isJarURL(url: URL): Boolean =
    protocols.contains(url.getProtocol)

  /** Enables URLConnection caches for JNLP connections.
   *
   * @param con the connection to configure
   */
  def useCachesIfNecessary(con: URLConnection): Unit =
    con.setUseCaches(con.getClass.getSimpleName.startsWith("JNLP"))

  /** Converts location string to URI (spaces to %20).
   *
   * @param location the location string
   * @return the URI
   */
  def toURI(location: String): URI =
    new URI(Strings.replace(location, " ", "%20"));

  /** Resolves the JAR file URL/path to a JarFile.
   *
   * @param jarFileUrl the JAR URL or path
   * @return the JarFile
   */
  def getJarFile(jarFileUrl: String): JarFile =
    if (jarFileUrl.startsWith("file:"))
      new JarFile(Jars.toURI(jarFileUrl).getSchemeSpecificPart())
    else
      new JarFile(jarFileUrl)
}
