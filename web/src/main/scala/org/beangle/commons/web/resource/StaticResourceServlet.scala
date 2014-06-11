package org.beangle.commons.web.resource

import javax.servlet.http.HttpServlet
import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import javax.servlet.ServletException
import org.beangle.commons.inject.Containers

class StaticResourceServlet extends HttpServlet {

  private var processor: ResourceProcessor = _

  override def init(config: ServletConfig) = {
    processor = Containers.getRoot().getBean(classOf[ResourceProcessor]).get;
  }

  @throws(classOf[Exception])
  override protected def service(request: HttpServletRequest, response: HttpServletResponse) {
    var uri = request.getRequestURI()
    var contextPath = request.getContextPath()
    if (!(contextPath.equals("") || contextPath.equals("/"))) {
      uri = Strings.substringAfter(uri, contextPath)
    }
    uri = uri.substring("/static".length())
    processor.process(uri, request, response)
  }
}