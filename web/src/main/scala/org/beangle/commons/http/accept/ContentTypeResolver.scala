package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType

trait ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType]
}