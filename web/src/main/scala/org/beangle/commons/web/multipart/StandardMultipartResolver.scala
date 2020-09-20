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

import jakarta.servlet.http.{HttpServletRequest, Part}
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings

import scala.collection.mutable

/** 标准的Multipart解析器
  * Standard Multipart Resolver
  */
object StandardMultipartResolver extends MultipartResolver {

  def isMultipart(request: HttpServletRequest): Boolean = {
    if ("post".equals(request.getMethod.toLowerCase)) {
      val contentType = request.getContentType
      contentType != null && contentType.toLowerCase.startsWith("multipart/")
    } else {
      false
    }
  }

  def resolve(request: HttpServletRequest): Map[String, Any] = {
    val partItor = request.getParts.iterator
    val params = new collection.mutable.HashMap[String, Any]
    while (partItor.hasNext) {
      val part = partItor.next
      if (part.getSize > 0) {
        val disposition = part.getHeader("content-disposition")
        if (disposition.contains("filename=")) {
          updatePart(params, part.getName, part)
        } else {
          val paramName = Strings.substringBetween(disposition, "name=\"", "\"")
          updateString(params, paramName, part)
        }
      } else {
        if (isFormField(part) && !params.contains(part.getName)) {
          params.put(part.getName, "")
        }
      }
    }
    params.toMap
  }

  /** 将字符类型的参数更新到参数表
    * @param params
    * @param name
    * @param part
    */
  def updateString(params: mutable.Map[String, Any], name: String, part: Part): Unit = {
    val b = new ByteArrayOutputStream
    IOs.copy(part.getInputStream, b)
    val str = new String(b.toByteArray)
    val newValue =
      params.get(name) match {
        case Some(v) =>
          v match {
            case a: Array[Any] => Array.concat(Array[Any](str), a)
            case _ => Array(v, str)
          }
        case None => str
      }
    params.put(name, newValue)
  }

  /** 将part类型的参数更新到参数表中
    * @param params
    * @param name
    * @param part
    */
  def updatePart(params: mutable.Map[String, Any], name: String, part: Part): Unit = {
    val newParts = params.get(name) match {
      case Some(arr) =>
        arr match {
          case _: String => Array(part)
          case a: Array[Part] => Array.concat(Array(part), a)
        }
      case None => Array(part)
    }
    params.put(name, newParts)
  }

  /** 是否一个普通的form表单域 */
  private def isFormField(part: Part): Boolean = {
    !("application/octet-stream" == part.getContentType || part.getHeader("content-disposition").contains("filename"))
  }

  override def cleanup(request: HttpServletRequest): Unit = {
    if (isMultipart(request)) {
      val partItor = request.getParts.iterator
      while (partItor.hasNext) partItor.next.delete()
    }
  }
}
