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
package org.beangle.commons.message.mail

import org.beangle.commons.message.Notifier
import scala.collection.JavaConversions._
import org.beangle.commons.message.Message
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings
import org.beangle.commons.message.NotificationException
import org.beangle.commons.lang.Throwables
import org.beangle.commons.logging.Logging

abstract class AbstractMailNotifier[T <: MailMessage] extends Notifier[T] with Logging {

  protected var mailSender: MailSender = _

  private var from: String = _

  def getType(): String = "mail"

  def deliver(msg: T) {
    beforeSend(msg)
    try {
      if (null == msg.getFrom() && null != getFrom()) msg.from(getFrom())
      mailSender.send(msg)
      afterSend(msg)
    } catch {
      case e: NotificationException =>
        logger.error("Cannot send message " + msg.getSubject(), e)
        Throwables.propagate(e)
    }
  }

  protected def buildSubject(msg: Message): String

  protected def buildText(msg: Message): String

  protected def beforeSend(msg: Message) {}

  protected def afterSend(msg: Message) {}

  def getMailSender(): MailSender = this.mailSender

  def setMailSender(mailSender: MailSender) {
    this.mailSender = mailSender
  }

  def getFrom(): String = this.from

  def setFrom(from: String) {
    Assert.notEmpty(from)
    this.from = from
  }
}
