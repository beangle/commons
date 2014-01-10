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

import scala.collection.Seq
import scala.beans.BeanProperty
import scala.collection.JavaConversions._

import java.util.LinkedHashMap
import java.io.UnsupportedEncodingException
import java.util.Date
import java.util.Properties

import javax.mail.Session
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeUtility
import javax.mail.Transport
import javax.mail.NoSuchProviderException
import javax.mail.AuthenticationFailedException

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.logging.Logging
import org.beangle.commons.message.NotificationException
import org.beangle.commons.message.NotificationSendException

object JavaMailSender {

  private val HEADER_MESSAGE_ID = "Message-ID"
}

import JavaMailSender._
class JavaMailSender extends MailSender with Logging {

  @BeanProperty
  var javaMailProperties: Properties = new Properties()

  var session: Session = _

  @BeanProperty
  var protocol: String = "smtp"

  @BeanProperty
  var host: String = _

  @BeanProperty
  var port: Int = -1

  @BeanProperty
  var username: String = _

  @BeanProperty
  var password: String = _

  @BeanProperty
  var defaultEncoding: String = _

  def send(messages: MailMessage*) {
    var mimeMsgs = new java.util.ArrayList[MimeMessage]()
    for (m <- messages) {
      try {
        mimeMsgs.add(createMimeMessage(m))
      } catch {
        case e: MessagingException => logger.error("Cannot mapping message" + m.getSubject(), e)
      }
    }
    doSend(mimeMsgs.toArray(new Array[MimeMessage](mimeMsgs.size)))
  }

  protected def createMimeMessage(mailMsg: MailMessage): MimeMessage = {
    var mimeMsg = new MimeMessage(getSession())

    mimeMsg.setSentDate(if (null == mailMsg.getSentAt()) new Date() else mailMsg.getSentAt())
    if (null != mailMsg.getFrom()) mimeMsg.setFrom(mailMsg.getFrom())
    try {
      addRecipient(mimeMsg, mailMsg)
    } catch {
      case e: MessagingException => Throwables.propagate(e)
    }
    var encoding = Strings.substringAfter(mailMsg.getContentType(), "charset=")
    try {
      mimeMsg.setSubject(MimeUtility.encodeText(mailMsg.getSubject(), encoding, "B"))
    } catch {
      case e: UnsupportedEncodingException => Throwables.propagate(e)
    }
    val text = mailMsg.getText()
    if (Strings.contains(mailMsg.getContentType(), "html")) {
      mimeMsg.setContent(text, if (Strings.isEmpty(encoding)) "text/html" else "text/htmlcharset=" + encoding)
    } else {
      mimeMsg.setText(text, if (Strings.isEmpty(encoding)) null else encoding)
    }
    mimeMsg
  }

  protected def getSession(): Session = {
    this.synchronized {
      if (this.session == null) this.session = Session.getInstance(this.javaMailProperties)
      this.session
    }
  }

  protected def getTransport(session: Session): Transport = {
    var protocol = getProtocol()
    if (protocol == null) protocol = session.getProperty("mail.transport.protocol")
    try {
      session.getTransport(protocol)
    } catch {
      case e: NoSuchProviderException => throw e
    }
  }

  protected def doSend(mimeMessages: Array[MimeMessage]) {
    var failedMessages = new LinkedHashMap[Object, Exception]
    var transport: Transport = null
    try {
      transport = getTransport(getSession())
      transport.connect(getHost(), getPort(), getUsername(), getPassword())
    } catch {
      case ex: AuthenticationFailedException => throw new NotificationException(ex.getMessage(), ex)
      case ex: MessagingException =>
        // Effectively, all messages failed...
        mimeMessages.foreach(original => failedMessages.put(original, ex))
        throw new NotificationException("Mail server connection failed", ex)
    }
    try {
      for (mimeMessage <- mimeMessages) {
        try {
          if (mimeMessage.getSentDate() == null) mimeMessage.setSentDate(new Date())
          var messageId = mimeMessage.getMessageID()
          mimeMessage.saveChanges()
          // Preserve explicitly specified message id...
          if (messageId != null) mimeMessage.setHeader(HEADER_MESSAGE_ID, messageId)
          transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients())
        } catch {
          case ex: MessagingException => failedMessages.put(mimeMessage, ex)
        }
      }
    } finally {
      try {
        transport.close()
      } catch {
        case ex: MessagingException =>
          throw if (!failedMessages.isEmpty()) new NotificationSendException("Failed to close server connection after message failures", ex,
            failedMessages)
          else throw new NotificationException("Failed to close server connection after message sending", ex)
      }
    }

    if (!failedMessages.isEmpty()) { throw new NotificationSendException(failedMessages) }
  }

  private def addRecipient(mimeMsg: MimeMessage, mailMsg: MailMessage): Int = {
    var recipients = 0
    try {
      for (to <- mailMsg.getTo()) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.TO, to)
        recipients += 1
      }
      for (cc <- mailMsg.getCc()) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.CC, cc)
        recipients += 1
      }
      for (bcc <- mailMsg.getBcc()) {
        mimeMsg.addRecipient(javax.mail.Message.RecipientType.BCC, bcc)
        recipients += 1
      }
    } catch { case e: MessagingException => Throwables.propagate(e) }
    return recipients
  }

  def setSession(session: Session) { this.session = session }
}
