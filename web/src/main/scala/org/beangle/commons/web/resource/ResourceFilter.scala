package org.beangle.commons.web.resource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

trait ResourceFilter {

  def filter( context:ProcessContext, request:HttpServletRequest, response:HttpServletResponse,
      chain:ProcessChain)

}