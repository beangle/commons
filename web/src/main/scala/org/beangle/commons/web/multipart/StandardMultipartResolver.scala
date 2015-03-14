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