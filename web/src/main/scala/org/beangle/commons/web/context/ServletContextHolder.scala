package org.beangle.commons.web.context

import javax.servlet.ServletContext

object ServletContextHolder {

  var context: ServletContext = _

  def store(servletContext: ServletContext): Unit = {
    context = servletContext
  }
}