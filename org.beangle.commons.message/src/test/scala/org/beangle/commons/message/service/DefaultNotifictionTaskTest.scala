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
package org.beangle.commons.message.service

import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import com.icegreen.greenmail.util.GreenMail
import org.testng.annotations.BeforeClass
import org.testng.annotations.AfterClass
import com.icegreen.greenmail.util.ServerSetupTest
import org.beangle.commons.message.mail.JavaMailSender
import org.beangle.commons.message.mail.DefaultMailNotifier
import org.beangle.commons.message.mail.MailMessage
import javax.mail.MessagingException
import org.beangle.commons.lang.Throwables
//remove if not needed
import scala.collection.JavaConversions._

@Test
class DefaultNotifictionTaskTest {
  private var greenMail: GreenMail = _

  @BeforeClass
  def setUp() {
    greenMail = new GreenMail(ServerSetupTest.ALL)
    // uses test ports by default
    greenMail.start()
    greenMail.setUser("test1@localhost", "user1", "password")
    greenMail.setUser("test2@localhost", "user2", "password")
  }

  @AfterClass
  def tearDown() {
    greenMail.stop()
  }

  def testMail() {
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
      assertEquals(1, msgs.length)
    } catch {
      case e: MessagingException => Throwables.propagate(e)
    }
  }
}