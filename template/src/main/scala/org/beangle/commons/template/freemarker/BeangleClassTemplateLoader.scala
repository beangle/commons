/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.template.freemarker

import freemarker.cache.URLTemplateLoader
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.ClassLoaders
import java.net.URL

object PrefixProcessor {

  /** Not starts with /,but end with / */
  def process(pre: String): String = {
    if (Strings.isBlank(pre)) return null
    var prefix = pre.trim()

    if (prefix.equals("/")) {
      null
    } else {
      if (!prefix.endsWith("/")) prefix += "/"
      if (prefix.startsWith("/")) prefix = prefix.substring(1)
      prefix
    }
  }
}

class BeangleClassTemplateLoader(prefixStr: String = null) extends URLTemplateLoader {

  val prefix = PrefixProcessor.process(prefixStr)

  protected def getURL(name: String): URL = {
    var url = ClassLoaders.getResource(name)
    if (null != prefix && url.isEmpty) url = ClassLoaders.getResource(prefix + name)
    url.orNull
  }

}
