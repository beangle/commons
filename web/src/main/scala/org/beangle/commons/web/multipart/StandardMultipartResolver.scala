/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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

import java.io.ByteArrayOutputStream

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part

/**
 * Standard Multipart Resolver
 */
object StandardMultipartResolver extends MultipartResolver {

  def isMultipart(request: HttpServletRequest): Boolean = {
    if (!"post".equals(request.getMethod.toLowerCase)) return false

    val contentType = request.getContentType
    (contentType != null && contentType.toLowerCase.startsWith("multipart/"))
  }

  def resolve(request: HttpServletRequest): Map[String, Any] = {
    val parts = request.getParts
    val partItor = request.getParts.iterator
    val params = new collection.mutable.HashMap[String, Any]
    while (partItor.hasNext()) {
      val part = partItor.next
      if (part.getSize > 0) {
        val disposition = part.getHeader("content-disposition")
        if (disposition.contains("filename=")) {
          val newParts = params.get(part.getName) match {
            case Some(arr) => Array.concat(Array(part), arr.asInstanceOf[Array[Part]])
            case None      => Array(part)
          }
          params.put(part.getName, newParts)
        } else {
          val paramName = Strings.substringBetween(disposition, "name=\"", "\"")
          val b = new ByteArrayOutputStream
          IOs.copy(part.getInputStream, b)
          val str = new String(b.toByteArray)
          params.put(paramName, str)
        }
      } else {
        params.put(part.getName, part)
      }
    }
    params.toMap
  }

  override def cleanup(request: HttpServletRequest): Unit = {
    if (isMultipart(request)) {
      val partItor = request.getParts().iterator
      while (partItor.hasNext) partItor.next.delete()
    }
  }
}
