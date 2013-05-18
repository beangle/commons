/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.web.access

import java.util.Collections
import java.util.LinkedList
import java.util.List
import java.util.Map
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.event.Event
import org.beangle.commons.event.EventListener
import org.beangle.commons.web.session.HttpSessionDestroyedEvent
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Memory access monitor.
 *
 * @author chaostone
 * @since 3.0.1
 */
class MemAccessMonitor extends AccessMonitor with EventListener[HttpSessionDestroyedEvent] {

  private var logger: AccessLogger = _

  private var builder: AccessRequestBuilder = _

  private var requests: Map[String, List[AccessRequest]] = CollectUtils.newConcurrentHashMap[String, List[AccessRequest]]

  def begin(request: HttpServletRequest): AccessRequest = {
    val r = builder.build(request)
    if (null != r) {
      var quene = requests.get(r.getSessionid)
      if (null == quene) {
        quene = Collections.synchronizedList(new LinkedList[AccessRequest]())
        requests.put(r.getSessionid, quene)
      }
      quene.add(r)
    }
    r
  }

  def end(request: AccessRequest, response: HttpServletResponse) {
    if (null == request) return
    val quene = requests.get(request.getSessionid)
    if (null != quene) quene.remove(request)
    if (null != logger) {
      request.setEndAt(System.currentTimeMillis())
      logger.log(request)
    }
  }

  def getRequests(): List[AccessRequest] = {
    val result = CollectUtils.newArrayList[AccessRequest](requests.size)
    for (quene <- requests.values) {
      result.addAll(quene)
    }
    result
  }

  def setLogger(logger: AccessLogger) {
    this.logger = logger
  }

  def setBuilder(builder: AccessRequestBuilder) {
    this.builder = builder
  }

  def onEvent(event: HttpSessionDestroyedEvent) {
    requests.remove(event.getSession.getId)
  }

  def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[HttpSessionDestroyedEvent].isAssignableFrom(eventType)
  }

  def supportsSourceType(sourceType: Class[_]): Boolean = true
}
