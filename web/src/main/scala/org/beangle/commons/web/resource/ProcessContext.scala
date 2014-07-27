package org.beangle.commons.web.resource

import java.net.URL

class ProcessContext(val uri: String, paths: List[String], urls: List[URL]) {

  val resources: List[Resource] = {
    val resources = new collection.mutable.ArrayBuffer[Resource]
    for (i <- 0 until paths.size) {
      resources += new Resource(paths(i), urls(i))
    }
    resources.toList
  }

}

class Resource(val path: String, val url: URL) {
}