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
package org.beangle.commons.message.mail

import java.util.List
import javax.mail.internet.InternetAddress
import org.testng.annotations.Test
import scala.collection.JavaConversions._
import org.testng.Assert

@Test
class MimeUtilsTest {

  def testParseAddress() {
    var me = "段体华<duantihua@gmail.com>,程序员<programer@gmail.com>"
    var adds = MimeUtils.parseAddress(me, "UTF-8")
    var i = 0
    for (add <- adds) {
      if (i == 0) {
        Assert.assertEquals("段体华", add.getPersonal())
      } else {
        Assert.assertEquals("程序员", add.getPersonal())
      }
      i += 1
    }
  }
}