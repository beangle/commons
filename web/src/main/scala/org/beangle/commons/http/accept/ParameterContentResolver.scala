package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType
import org.beangle.commons.activation.MimeTypeProvider

class ParameterContentResolver(val parameterName: String) extends ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    val ext = request.getParameter(parameterName)
    if (null == ext) Seq.empty
    else {
      MimeTypeProvider.getMimeType(ext) match {
        case Some(mimeType) => List(mimeType)
        case None => Seq.empty
      }
    }
  }
}