package org.beangle.commons.web.security

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.security.Request
import org.beangle.commons.web.util.RequestUtils
import org.beangle.commons.security.DefaultRequest

trait RequestConvertor {

  def convert(request: HttpServletRequest): Request
}

class DefaultRequestConvertor extends RequestConvertor {
  def convert(request: HttpServletRequest): Request = {
    new DefaultRequest(RequestUtils.getServletPath(request.asInstanceOf[HttpServletRequest]), request.getMethod())
  }
}