/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.resource

import org.beangle.commons.activation.MimeTypeProvider
import org.beangle.commons.io.{ IOs, ResourceLoader }
import org.beangle.commons.lang.Strings.substringAfterLast

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class ResourceProcessor(private val loader: ResourceLoader, private val resolver: PathResolver) {

  var filters: List[ResourceFilter] = List.empty

  def process(uri: String, request: HttpServletRequest, response: HttpServletResponse) {
    val names = resolver.resolve(uri)
    val resources = loader.load(names)
    if (resources.size != names.size) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND)
    } else {
      val pc = new ProcessContext(uri, names, resources)
      response.setContentType(getContentType(pc.uri, request))

      val chain = new ProcessChain(filters.iterator)
      chain.process(pc, request, response)
      if (response.getStatus() == HttpServletResponse.SC_OK) {
        val isText = response.getContentType.startsWith("text/")
        val os = response.getOutputStream
        for (res <- pc.resources) {
          val is = res.url.openStream()
          IOs.copy(is, os)
          is.close()
          if (isText) os.write('\n')
        }
      }
    }
  }

  protected def getContentType(uri: String, request: HttpServletRequest): String = {
    val contentType = MimeTypeProvider.getMimeType(substringAfterLast(uri, ".")).orNull
    if (null == contentType) request.getServletContext().getMimeType(uri) else contentType.toString
  }

}