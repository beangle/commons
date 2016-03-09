/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.web.resource.impl

import org.beangle.commons.web.resource.PathResolver
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import scala.collection.mutable.ListBuffer

class PathResolverImpl extends PathResolver {
  override def resolve(path: String): List[String] = {
    val lastPostfix = "." + Strings.substringAfterLast(path, ".")
    val names = Strings.split(path, ",")
    val rs = new ListBuffer[String]()
    var pathDir: String = null
    for (name <- names) {
      var iname = name
      if (iname.startsWith("/")) {
        pathDir = Strings.substringBeforeLast(name, "/").substring(1)
        iname = iname.substring(1)
      } else iname = pathDir + "/" + iname
      if (!iname.endsWith(lastPostfix)) iname = iname + lastPostfix
      rs += iname
    }
    return rs.toList
  }
}