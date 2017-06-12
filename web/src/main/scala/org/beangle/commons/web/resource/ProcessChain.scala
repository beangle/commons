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
package org.beangle.commons.web.resource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer
import org.beangle.commons.lang.Arrays
import org.beangle.commons.io.IOs
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.io.ByteArrayOutputStream

class ProcessChain(filters: Iterator[ResourceFilter]) {

  def process(context: ProcessContext, request: HttpServletRequest, response: HttpServletResponse) {
    if (filters.hasNext) {
      filters.next().filter(context, request, response, this)
    }
  }
}