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
package org.beangle.commons.collection.page

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import java.util.ArrayList
import java.util.List
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

/**
 * @author zhouqi
 */
class PageAdapterTest {

  @Test
  def testD() {
    val datas = new ArrayList[String](26)
    for (i <- 0 until 26) {
      datas.add(String.valueOf(i))
    }
    val page = new PagedList[String](datas, 20)
    assertNotNull(page.iterator())
    assertEquals(page.iterator().next(), "0")
    page.next()
    assertEquals(page.iterator().next(), "20")
    page.moveTo(2)
    assertEquals(page.iterator().next(), "20")
    page.previous()
    assertEquals(page.iterator().next(), "0")
  }
}
