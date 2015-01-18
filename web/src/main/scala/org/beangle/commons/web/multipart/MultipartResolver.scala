package org.beangle.commons.web.multipart

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part

trait MultipartResolver {

  def isMultipart(request: HttpServletRequest): Boolean

  def resolve(request: HttpServletRequest): Map[String, Array[Part]]

  def cleanup(request: HttpServletRequest)
}