/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.message

import java.util.Map
import java.util.LinkedHashMap
import scala.beans.BeanProperty

//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(-4019257253565582587L)
class NotificationSendException(message: String, cause: Throwable) extends NotificationException(message, cause) {

  private var failedMessages: Map[Object, Exception] = _

  def this(message: String, cause: Throwable, failedMessages: Map[Object, Exception]) {
    this(message, cause)
    this.failedMessages = new LinkedHashMap[Object, Exception](failedMessages)
  }

  def this(failedMessages: Map[Object, Exception]) {
    this(null, null, failedMessages)
  }

  def getFailedMessages(): Map[Object, Exception] = this.failedMessages

}