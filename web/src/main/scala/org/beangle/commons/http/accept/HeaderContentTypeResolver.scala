package org.beangle.commons.http.accept

import javax.activation.MimeType
import javax.servlet.http.HttpServletRequest

class HeaderContentTypeResolver extends ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    Seq.empty
  }
}