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

import org.beangle.commons.message.AbstractMessage
import javax.mail.internet.InternetAddress
import java.util.List
import java.util.Date
import scala.beans.BeanProperty
import org.beangle.commons.lang.Strings
import java.util.ArrayList
import scala.collection.JavaConversions._
import org.beangle.commons.lang.Assert

class MailMessage extends AbstractMessage {

  private var from: InternetAddress = null

  private var to: List[InternetAddress] = new ArrayList()

  private var cc: List[InternetAddress] = new ArrayList()

  private var bcc: List[InternetAddress] = new ArrayList()

  private var sentAt: Date = _

  def getEncoding(): String = Strings.substringAfter(getContentType(), "charset=")

  def getRecipients(): List[String] = {
    var recipients = new ArrayList[String]
    this.to.foreach(a => recipients.add(a.toString()))
    this.cc.foreach(a => recipients.add(a.toString()))
    this.bcc.foreach(a => recipients.add(a.toString()))
    recipients
  }

  def this(subject: String, text: String, sendTo: String) {
    this()
    this.to = MimeUtils.parseAddress(sendTo, getEncoding())
    setSubject(subject)
    setText(text)
  }

  def this(subject: String, text: String, sendTo: String, sendCc: String, sendBcc: String) {
    this()
    this.to = MimeUtils.parseAddress(sendTo, getEncoding())
    this.cc = MimeUtils.parseAddress(sendCc, getEncoding())
    this.bcc = MimeUtils.parseAddress(sendBcc, getEncoding())
    setSubject(subject)
    setText(text)
  }

  def getTo(): List[InternetAddress] = this.to

  def getCc(): List[InternetAddress] = this.cc

  def getBcc(): List[InternetAddress] = this.bcc

  def from(from: String): MailMessage = {
    var froms = MimeUtils.parseAddress(from, getEncoding())
    if (froms.size() > 0) this.from = froms.get(0)
    this
  }

  def getFrom(): InternetAddress = this.from

  def addTo(sendTo: String) {
    Assert.notNull(sendTo)
    this.to.addAll(MimeUtils.parseAddress(sendTo, getEncoding()))
  }

  def addCc(sendCc: String) {
    Assert.notNull(sendCc)
    this.cc.addAll(MimeUtils.parseAddress(sendCc, getEncoding()))
  }

  def addBcc(sendBcc: String) {
    Assert.notNull(sendBcc)
    this.bcc.addAll(MimeUtils.parseAddress(sendBcc, getEncoding()))
  }

  def getSentAt(): Date = this.sentAt

  def setSentAt(sendAt: Date) {
    this.sentAt = sendAt
  }
}
