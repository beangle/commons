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
package org.beangle.commons.io

import java.net.URL
import java.net.URLConnection
import org.beangle.commons.lang.Strings
import java.net.URI
import java.util.jar.JarFile

object Jars {

  val protocols = Set("jar", "zip", "wsjar", "vsfzip")

  val URLSeparator = "!/"

  def isJarURL(url: URL): Boolean = {
    protocols.contains(url.getProtocol)
  }

  def useCachesIfNecessary(con: URLConnection): Unit = {
    con.setUseCaches(con.getClass.getSimpleName.startsWith("JNLP"))
  }

  def toURI(location: String): URI = {
    new URI(Strings.replace(location, " ", "%20"));
  }

  /**
   * Resolve the given jar file URL into a JarFile object.
   */
  def getJarFile(jarFileUrl: String): JarFile = {
    if (jarFileUrl.startsWith("file:")) {
      new JarFile(Jars.toURI(jarFileUrl).getSchemeSpecificPart())
    } else {
      new JarFile(jarFileUrl)
    }
  }
}