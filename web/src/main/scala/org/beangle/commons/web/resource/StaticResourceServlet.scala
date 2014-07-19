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

  override def init(config: ServletConfig): Unit = {
    processor = Containers.root.getBean(classOf[ResourceProcessor]).get
  }

  @throws(classOf[Exception])
  override protected def service(request: HttpServletRequest, response: HttpServletResponse) {
    val contextPath = request.getContextPath()
    val uri =
      if (!(contextPath.equals("") || contextPath.equals("/"))) {
        Strings.substringAfter(request.getRequestURI, contextPath)
      } else request.getRequestURI
    processor.process(uri.substring("/static".length), request, response)
  }
}