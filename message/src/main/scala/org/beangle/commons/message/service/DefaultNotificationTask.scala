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
package org.beangle.commons.message.service

import org.beangle.commons.logging.Logging
import org.beangle.commons.message.NotificationTask
import org.beangle.commons.message.Message
import org.beangle.commons.message.MessageQueue
import scala.beans.BeanProperty
import org.beangle.commons.message.Notifier
import org.beangle.commons.message.NotificationException
//remove if not needed
import scala.collection.JavaConversions._

class DefaultNotificationTask[T <: Message] extends NotificationTask[T] with Logging {

  private var queue: MessageQueue[T] = new DefaultMessageQueue[T]

  private var notifier: Notifier[T] = _

  private var observer: SendingObserver = _

  private var taskInterval: Long = 0

  def getMessageQueue(): MessageQueue[T] = queue

  def setMessageQueue(messageQueue: MessageQueue[T]) {
    this.queue = messageQueue
  }

  def getNotifier(): Notifier[T] = notifier

  def setNotifier(notifier: Notifier[T]) {
    this.notifier = notifier
  }

  def send() {
    var msg = queue.poll()
    while (null != msg) {
      try {
        if (null != observer) observer.onStart(msg)
        notifier.deliver(msg)
        if (taskInterval > 0) Thread.sleep(taskInterval)
      } catch {
        case e: NotificationException => logger.error("send error", e)
        case e: InterruptedException => logger.error("send error", e)
      }
      if (null != observer) observer.onFinish(msg)
      msg = queue.poll()
    }
  }

  def setObserver(observer: SendingObserver) {
    this.observer = observer
  }

}
