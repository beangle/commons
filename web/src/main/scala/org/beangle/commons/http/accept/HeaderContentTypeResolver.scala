package org.beangle.commons.http.accept

import javax.activation.MimeType
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.activation.MimeTypes

class HeaderContentTypeResolver extends ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    MimeTypes.parse(request.getHeader("Accept"))
  }

}