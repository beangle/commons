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
      if (iname.startsWith("/")) pathDir = Strings.substringBeforeLast(name, "/")
      else iname = pathDir + "/" + iname
      if (!iname.endsWith(lastPostfix)) iname = iname + lastPostfix
      rs += iname
    }
    return rs.toList
  }
}