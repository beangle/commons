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

import org.beangle.commons.lang.Strings.{split, substringAfter, substringBetween}

import java.net.URL
import scala.util.boundary.break

object ResourceVersionLoader {
  def getResource(resourceName: String, version: String): Option[URL] = {
    new ResourceVersionLoader().getResource(resourceName, version)
  }
}

/** Get resources by version
  * matched order
  * {{{
  * 1 path/to/file_version.txt
  * 2 path/to/file(start_version~end_version).txt
  * 3 path/to/file.txt
  * }}}
  *
  * @param loader
  */
class ResourceVersionLoader(loader: ResourceLoader = new ClasspathResourceLoader) {
  private val patternResolver = new ResourcePatternResolver(loader)

  def getResource(resourceName: String, version: String): Option[URL] = {
    val idxLastDot = resourceName.lastIndexOf('.')
    var path = resourceName
    var suffix = ""
    if (idxLastDot > -1) {
      path = resourceName.substring(0, idxLastDot)
      suffix = resourceName.substring(idxLastDot)
    }
    val versioned = loader.load(path + "_" + version + suffix)
    if (versioned.nonEmpty) versioned
    else
      val urls = patternResolver.getResources(path + "*" + suffix)
      var defaultUrl: Option[URL] = None
      var matched: Option[URL] = None
      for (url <- urls if matched.isEmpty) {
        val fullPath = url.getFile
        if (fullPath.endsWith(resourceName)) defaultUrl = Some(url)
        else
          val v = substringAfter(fullPath, path)
          if (v.charAt(0) == '(') {
            val scheme = substringBetween(v, "(", ")")
            val vp = split(scheme, "~")
            if vp.length == 2 && version.compareTo(vp(0)) >= 0 && version.compareTo(vp(1)) <= 0 then matched = Some(url)
          }
      }

      matched.orElse(defaultUrl)
  }
}
