package org.beangle.commons.web.multipart

import javax.servlet.http.HttpServletRequest
import sun.rmi.runtime.Log.LogFactory
import javax.servlet.http.Part
import org.beangle.commons.logging.Logging

class StandardMultipartResolver extends MultipartResolver with Logging {

  //  val fileNameKey = "filename="

  def isMultipart(request: HttpServletRequest): Boolean = {
    if (!"post".equals(request.getMethod().toLowerCase())) return false

    val contentType = request.getContentType
    return (contentType != null && contentType.toLowerCase().startsWith("multipart/"))
  }

  def resolve(request: HttpServletRequest): MultipartRequest = {
    val parts = request.getParts
    val partItor = request.getParts.iterator()
    val files = new collection.mutable.HashMap[String, List[Part]]
    while (partItor.hasNext()) {
      val part = partItor.next
      val filename = part.getSubmittedFileName
      if (filename != null) {
        val newParts = files.get(part.getName) match {
          case Some(list) => part :: list
          case None => List(part)
        }
        files.put(part.getName, newParts)
      }
    }
    new DefaultMultipartRequest(files.toMap, request)
  }

  //  private def extractFilename(contentDisposition: String): String = {
  //    if (contentDisposition == null) return null
  //    val startIndex = contentDisposition.indexOf(fileNameKey)
  //    if (startIndex == -1) return null
  //
  //    val filename = contentDisposition.substring(startIndex + fileNameKey.length())
  //    if (filename.startsWith("\"")) {
  //      val endIndex = filename.indexOf("\"", 1)
  //      if (endIndex != -1) filename.substring(1, endIndex) else filename
  //    } else {
  //      val endIndex = filename.indexOf(";")
  //      if (endIndex != -1) return filename.substring(0, endIndex) else filename
  //    }
  //  }

  override def cleanup(request: MultipartRequest): Unit = {
    val partItor = request.getParts().iterator()
    while (partItor.hasNext()) {
      partItor.next.delete()
    }
  }
}