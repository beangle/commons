package org.beangle.commons.web.resource.filter

import org.beangle.commons.web.resource.ResourceFilter
import scala.collection.mutable.HashMap
import org.beangle.commons.web.resource.ProcessContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.web.resource.ProcessChain
import org.beangle.commons.lang.Strings

class ContentTypeFilter extends ResourceFilter {
  /**
   * Servered content types
   */
  private var contentTypes: HashMap[String, String] = new HashMap[String, String]

  contentTypes += ("js" -> "text/javascript")
  contentTypes += ("css" -> "text/css")
  contentTypes += ("html" -> "text/html")
  contentTypes += ("htm" -> "text/html")
  contentTypes += ("txt" -> "text/plain")
  contentTypes += ("gif" -> "image/gif")
  contentTypes += ("jpg" -> "image/jpeg")
  contentTypes += ("jpeg" -> "image/jpeg")
  contentTypes += ("png" -> "image/png")
  contentTypes += ("json" -> "application/json")
  contentTypes += ("htc" -> "text/x-component")

  override def filter(context: ProcessContext, request: HttpServletRequest, response: HttpServletResponse,
    chain: ProcessChain) {
    val contentType = contentTypes.get(Strings.substringAfterLast(context.uri, ".")).get
    if (contentType != null) response.setContentType(contentType)
    chain.process(context, request, response)
  }

  def setContentTypes(contentTypes: HashMap[String, String]) {
    this.contentTypes = contentTypes
  }
}