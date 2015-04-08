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
package org.beangle.commons.web.multipart

import javax.servlet.http.HttpServletRequest
import sun.rmi.runtime.Log.LogFactory
import javax.servlet.http.Part

object StandardMultipartResolver extends MultipartResolver {

  def isMultipart(request: HttpServletRequest): Boolean = {
    if (!"post".equals(request.getMethod.toLowerCase)) return false

    val contentType = request.getContentType
    return (contentType != null && contentType.toLowerCase.startsWith("multipart/"))
  }

  def resolve(request: HttpServletRequest): Map[String, Array[Part]] = {
    val parts = request.getParts
    val partItor = request.getParts.iterator()
    val files = new collection.mutable.HashMap[String, Array[Part]]
    while (partItor.hasNext()) {
      val part = partItor.next
      if (part.getSize > 0) {
        if (part.getHeader("content-disposition").contains("filename=")) {
          val newParts = files.get(part.getName) match {
            case Some(arr) => Array.concat(Array(part), arr)
            case None => Array(part)
          }
          files.put(part.getName, newParts)
        }
      }
    }
    files.toMap
  }

  override def cleanup(request: HttpServletRequest): Unit = {
    if (isMultipart(request)) {
      val partItor = request.getParts().iterator
      while (partItor.hasNext) {
        partItor.next.delete()
      }
    }
  }
}