package org.beangle.commons.web.resource.filter

import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.lang.Strings.substringAfterLast
import org.beangle.commons.web.resource.{ ProcessChain, ProcessContext, ResourceFilter }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class ContentTypeFilter extends ResourceFilter {

  var provider: MimeTypeProvider = _

  override def filter(context: ProcessContext, req: HttpServletRequest, res: HttpServletResponse, chain: ProcessChain) {
    provider.getMimeType(substringAfterLast(context.uri, ".")) foreach { ct => res.setContentType(ct) }
    chain.process(context, req, res)
  }
}