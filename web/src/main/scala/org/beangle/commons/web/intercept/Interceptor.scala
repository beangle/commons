package org.beangle.commons.web.intercept

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.lang.primitive.MutableInt

trait Interceptor {

  def preInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean

  def postInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit
}

trait OncePerRequestInterceptor extends Interceptor {
  final val attributeName = getClass.getName + "_count"

  override final def preInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    var count = request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (null == count) {
      count = new MutableInt
      request.setAttribute(attributeName, count)
    }
    if (count.increment() == 1) doPreInvoke(request, response) else true
  }

  def doPreInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    true
  }

  def doPostInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {

  }

  override final def postInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    var count = request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (count.decrement() == 0) doPostInvoke(request, response)
  }
}
