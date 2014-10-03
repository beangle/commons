package org.beangle.commons.http.accept

import javax.servlet.http.HttpServletRequest
import javax.activation.MimeType

class ContentNegotiationManager(val resolvers:Seq[ContentTypeResolver]) {

  def resolve(request: HttpServletRequest): Seq[MimeType]={
    //TODO 
    Seq.empty
  }
}