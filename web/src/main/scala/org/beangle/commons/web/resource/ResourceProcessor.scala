package org.beangle.commons.web.resource

import org.beangle.commons.io.ResourceLoader
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ResourceProcessor {
  var loader: ResourceLoader = _

  var resolver: PathResolver = _

  var filters: List[ResourceFilter] = _

  def process(uri: String, request: HttpServletRequest, response: HttpServletResponse) {
    val names = resolver.resolve(uri)
    val resources = loader.load(names)
    if (resources.size != names.size) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND)
    } else {
      val pc = new ProcessContext(uri, names, resources)
      val chain = new ProcessChain(filters.iterator)
      chain.process(pc, request, response)
      if (response.getStatus() == HttpServletResponse.SC_OK) {
        val isText = null != response.getContentType() && response.getContentType().startsWith("text/")
        for (res <- pc.resources) {
          response.getOutputStream().write(res.data)
          if (isText) response.getOutputStream().write('\n')
        }
      }
    }
  }

}