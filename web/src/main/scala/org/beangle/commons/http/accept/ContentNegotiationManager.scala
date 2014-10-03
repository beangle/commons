package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType

class ContentNegotiationManager(val resolvers: Seq[ContentTypeResolver]) {

  def resolve(request: HttpServletRequest): Seq[MimeType] = {
    val iter = resolvers.iterator
    while (iter.hasNext) {
      val resolver = iter.next()
      val mimeTypes = resolver.resolve(request)
      if (!mimeTypes.isEmpty) return mimeTypes
    }
    Seq.empty
  }
}