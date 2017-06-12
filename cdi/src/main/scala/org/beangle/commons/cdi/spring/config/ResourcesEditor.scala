/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.cdi.spring.config

import java.beans.PropertyEditorSupport
import java.io.IOException
import java.net.URL
import org.beangle.commons.config.Resources
import org.beangle.commons.lang.Strings
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.beangle.commons.io.ResourcePatternResolver

class ResourcesEditor extends PropertyEditorSupport {

  private val resourceResolver = new ResourcePatternResolver

  private def getResource(location: String): Option[URL] = {
    if (Strings.isBlank(location)) return None
    val resourceList = resourceResolver.getResources(location)
    if (resourceList.isEmpty) None else Some(resourceList.head)
  }

  private def getResources(locationPattern: String): List[URL] = {
    resourceResolver.getResources(locationPattern)
  }

  override def setAsText(text: String) {
    if (Strings.isNotBlank(text)) {
      val paths = text.split(";")
      var global: Option[URL] = None
      var locals: List[URL] = List.empty
      var user: Option[URL] = None
      if (paths.length > 0) global = getResource(paths(0))
      if (paths.length > 1) locals = getResources(paths(1))
      if (paths.length > 2) user = getResource(paths(2))
      setValue(new Resources(global, locals, user))
    } else {
      setValue(null)
    }
  }
}
