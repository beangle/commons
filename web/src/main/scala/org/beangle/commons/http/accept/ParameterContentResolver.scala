package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType

class ParameterContentResolver extends ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    Seq.empty
  }
}