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
package org.beangle.commons.io

import java.io.IOException
import java.net.URL
import java.util.Enumeration
import org.beangle.commons.logging.Logging
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.ClassLoaders
import scala.collection.mutable.ListBuffer

/**
 * Load resource by class loader.
 *
 * @author chaostone
 * @since 4.0.1
 */
class ClasspathResourceLoader(val prefixes: List[String] = List("")) extends ResourceLoader with Logging {

  def this(prefixStr: String) {
    this(if (Strings.isEmpty(prefixStr)) List("") else Strings.split(prefixStr, " ").toList)
  }

  def loadAll(resourceName: String): List[URL] = {
    val urls = new collection.mutable.ListBuffer[URL]
    for (prefix <- prefixes)
      urls ++= ClassLoaders.getResources(prefix + resourceName, getClass())
    urls.toList
  }

  def load(name: String): Option[URL] = {
    var url: URL = null
    for (prefix <- prefixes; if (null == url)) {
      url = ClassLoaders.getResource(prefix + name, getClass());
    }
    if (null == url) None
    else Some(url)
  }

  def load(names: Seq[String]): List[URL] = {
    val urls = new collection.mutable.ListBuffer[URL]
    for (name <- names) {
      val url = load(name)
      if (!url.isEmpty) urls += url.get
    }
    urls.toList
  }

}
