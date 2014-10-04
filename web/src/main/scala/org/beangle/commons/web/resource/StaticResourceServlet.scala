package org.beangle.commons.web.resource

import org.beangle.commons.inject.Container
import org.beangle.commons.io.ClasspathResourceLoader
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.resource.filter.HeaderFilter
import org.beangle.commons.web.resource.impl.PathResolverImpl

import javax.servlet.ServletConfig
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

class StaticResourceServlet extends HttpServlet {

  private var processor: ResourceProcessor = _

  override def init(config: ServletConfig): Unit = {
    processor = buildProcessor()
  }

  @throws(classOf[Exception])
  override protected def service(request: HttpServletRequest, response: HttpServletResponse) {
    val contextPath = request.getContextPath
    val uri =
      if (!(contextPath.equals("") || contextPath.equals("/"))) {
        Strings.substringAfter(request.getRequestURI, contextPath)
      } else request.getRequestURI
    processor.process(uri, request, response)
  }

  protected def buildProcessor(): ResourceProcessor = {
    Container.ROOT.getBean(classOf[ResourceProcessor]) match {
      case Some(p) => p
      case None =>
        val p = new ResourceProcessor(new ClasspathResourceLoader, new PathResolverImpl())
        p.filters = List(new HeaderFilter)
        p
    }
  }
}