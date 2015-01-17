package org.beangle.commons.web.multipart

import javax.servlet.http.HttpServletRequest

trait MultipartResolver {

  def isMultipart(request: HttpServletRequest): Boolean

  def resolve(request: HttpServletRequest): MultipartRequest

  def cleanup(request: MultipartRequest)
}