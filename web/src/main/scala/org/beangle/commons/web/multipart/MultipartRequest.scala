package org.beangle.commons.web.multipart

import javax.servlet.http.{ HttpServletRequest, HttpServletRequestWrapper }
import java.util.Collections
import scala.collection.mutable.LinkedHashSet
import javax.servlet.http.Part

trait MultipartRequest extends HttpServletRequest {

  def partNames: List[String]

  def getParts(name: String): List[Part]

  def partMap: Map[String, List[Part]]
}

class DefaultMultipartRequest(val partMap: Map[String, List[Part]], request: HttpServletRequest)
  extends HttpServletRequestWrapper(request) with MultipartRequest {

  def partNames: List[String] = {
    partMap.keys.toList
  }

  def getParts(name: String): List[Part] = {
    partMap.get(name) match {
      case Some(list) => list
      case None => List.empty
    }
  }

  /**
   * Servlet 3.0 getParameterNames() not guaranteed to include multipart form items
   * need to merge them here to be on the safe side
   */
  override def getParameterNames(): java.util.Enumeration[String] = {
    if (partMap.isEmpty) return super.getParameterNames

    val paramNames = new LinkedHashSet[String]
    val paramEnum = super.getParameterNames();
    while (paramEnum.hasMoreElements()) {
      paramNames.add(paramEnum.nextElement());
    }
    paramNames ++= partMap.keys
    Collections.enumeration(collection.JavaConversions.asJavaCollection(paramNames))
  }

  override def getParameterMap(): java.util.Map[String, Array[String]] = {
    if (partMap.isEmpty) return super.getParameterMap

    val paramMap = new java.util.LinkedHashMap[String, Array[String]]
    paramMap.putAll(super.getParameterMap())
    for (paramName <- partMap.keys) {
      if (!paramMap.containsKey(paramName)) {
        paramMap.put(paramName, getParameterValues(paramName))
      }
    }
    paramMap
  }
}