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
package org.beangle.commons.collection

import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import java.sql.Date
import java.util.Calendar
import java.util.Map
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class MapConverterTest {

  var datas: Map[String, Any] = CollectUtils.newHashMap()

  var converter: MapConverter = new MapConverter()

  @BeforeClass
  def setUp() {
    datas.put("empty1", "")
    datas.put("empty2", null)
    datas.put("empty3", Array(""))
  }

  def testGetDate() {
    val year = 2010
    val month = 9
    val day = 1
    datas.put("birthday", year + "-" + month + "-" + day)
    val birthday = converter.get(datas, "birthday", classOf[Date])
    val calendar = Calendar.getInstance
    calendar.setTime(birthday)
    assertEquals(calendar.get(Calendar.YEAR), year)
    assertEquals(calendar.get(Calendar.MONTH), month - 1)
    assertEquals(calendar.get(Calendar.DAY_OF_MONTH), day)
    datas.put("birthday", Array(birthday))
    val birthday2 = converter.get(datas, "birthday", classOf[Date])
    assertEquals(birthday, birthday2)
  }

  def testGetArray() {
    datas.put("name", Array("me"))
    val name = converter.get(datas, "name")
    assertNotNull(name)
    assertEquals(name, "me")
  }

  def testGetNull() {
    var empty1 = converter.getBool(datas, "empty1")
    assertFalse(empty1)
    empty1 = converter.getBool(datas, "empty2")
    assertFalse(empty1)
    var emptyB1 = converter.getBoolean(datas, "empty1")
    assertNull(emptyB1)
    emptyB1 = converter.getBoolean(datas, "empty2")
    assertNull(emptyB1)
    var id = converter.getLong(datas, "empty1")
    assertNull(id)
    id = converter.getLong(datas, "empty2")
    assertNull(id)
    id = converter.getLong(datas, "empty3")
    assertNull(id)
  }
}
