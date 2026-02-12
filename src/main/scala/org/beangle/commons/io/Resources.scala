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

import org.beangle.commons.lang.ClassLoaders.{getResource, getResources}
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.net.Networks

import java.io.File
import java.net.URL

/** Resource loading (classpath, file, http). */
object Resources {

  /** Loads resources from comma-separated paths. Supports class://, classpath:, classpath*:, file://, http.
   *
   * @param paths comma-separated resource paths
   * @return iterable of URLs
   */
  def load(paths: String): Iterable[URL] = {
    Strings.split(paths, ",").flatMap(p => doLoad(p))
  }

  private def doLoad(path: String): Iterable[URL] = {
    if (path.startsWith("class://")) {
      getResource(path.substring("class://".length))
    } else if (path.startsWith("classpath:")) {
      getResource(path.substring("classpath:".length))
    } else if (path.startsWith("classpath*:")) {
      getResources(path.substring("classpath*:".length)) // multiple values
    } else if (path.startsWith("http")) {
      Some(Networks.url(path))
    } else if (path.startsWith("file://")) {
      Some(new File(path).toURI.toURL)
    } else {
      getResource(path)
    }
  }
}
