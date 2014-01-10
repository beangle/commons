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

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import org.beangle.commons.message.mail.JavaMailSender
import org.beangle.commons.message.mail.DefaultMailNotifier
import org.beangle.commons.message.mail.MailMessage
import javax.mail.MessagingException
import org.beangle.commons.lang.Throwables
//remove if not needed
import scala.collection.JavaConversions._
import org.scalatest.FunSpec
import org.scalatest.Matchers

class DefaultNotifictionTaskTest extends FunSpec with Matchers {
  private var greenMail = new GreenMail(ServerSetupTest.ALL)
  greenMail.start()
  greenMail.setUser("test1@localhost", "user1", "password")
  greenMail.setUser("test2@localhost", "user2", "password")

  describe("JavaMailSender") {
    it("testMail") {
      try {
        var mailSender = new JavaMailSender()
        mailSender.setHost("localhost")
        mailSender.setUsername("user1")
        mailSender.setPassword("password")
        mailSender.setPort(3025)

        var notifier = new DefaultMailNotifier[MailMessage](mailSender)
        notifier.setFrom("测试name<user1@localhost>")
        var task = new DefaultNotificationTask[MailMessage]()
        task.setNotifier(notifier)
        var mmc = new MailMessage("测试", "测试简单邮件发送机制", "user2@localhost")
        task.getMessageQueue().addMessage(mmc)
        task.send()
        var msgs = greenMail.getReceivedMessages()
        msgs.length should be(1)
        greenMail.stop()
      } catch {
        case e: MessagingException => Throwables.propagate(e)
      }
    }
  }

}
