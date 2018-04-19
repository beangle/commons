/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
          val newValue =
            params.get(paramName) match {
              case Some(v) =>
                if (v.getClass.isArray) {
                  Array.concat(Array(str), v.asInstanceOf[Array[String]])
                } else {
                  Array(v, str)
                }
              case None => str
            }
          params.put(paramName, newValue)
        }
      } else {
        if (isFormField(part) && !params.contains(part.getName)) {
          params.put(part.getName, "")
        }
      }
    }
    params.toMap
  }

  private def isFormField(part: Part): Boolean = {
    val isFile = ("application/octet-stream" == part.getContentType || part.getHeader("content-disposition").contains("filename"))
    !isFile
  }
  override def cleanup(request: HttpServletRequest): Unit = {
    if (isMultipart(request)) {
      val partItor = request.getParts().iterator
      while (partItor.hasNext) partItor.next.delete()
    }
  }
}
