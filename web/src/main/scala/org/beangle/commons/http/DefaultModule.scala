/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.http

import org.beangle.commons.http.mime.MimeTypeProvider
import org.beangle.commons.inject.bind.AbstractBindModule

/**
 * @author chaostone
 */
class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[MimeTypeProvider]).property("resources", "classpath:org/beangle/commons/http/mime/mimetypes.properties;classpath*:META-INF/mimetypes.properties;")
  }
}
