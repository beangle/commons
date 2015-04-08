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
package org.beangle.commons.web.access

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils

/**
 * Default access request builder
 *
 * @author chaostone
 * @since 3.0.1
 */
class DefaultAccessRequestBuilder extends AccessRequestBuilder {

  def build(request: HttpServletRequest): AccessRequest = {
    var ar: AccessRequest = null
    val session = request.getSession(false)
    if (null != session) {
      val sessionid = session.getId
      val username = abtainUsername(request)
      if (Strings.isNotEmpty(username)) {
        ar = new AccessRequest(sessionid, username, RequestUtils.getServletPath(request))
        ar.params = request.getQueryString
      }
    }
    ar
  }

  /**
   * Return remote user name
   */
  protected def abtainUsername(request: HttpServletRequest): String = request.getRemoteUser
}
