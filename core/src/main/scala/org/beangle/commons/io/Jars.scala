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