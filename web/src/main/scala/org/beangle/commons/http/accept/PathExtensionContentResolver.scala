package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType
import org.beangle.commons.activation.MimeTypeProvider
import org.beangle.commons.web.util.RequestUtils
import org.beangle.commons.lang.Strings

class PathExtensionContentResolver extends ContentTypeResolver {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    val servletPath = RequestUtils.getServletPath(request)
    val ext = Strings.substringAfterLast(servletPath, ".")
    if (ext.length == 0) Seq.empty
    else {
      MimeTypeProvider.getMimeType(ext) match {
        case Some(mimeType) => List(mimeType)
        case None => Seq.empty
      }
    }
  }
}