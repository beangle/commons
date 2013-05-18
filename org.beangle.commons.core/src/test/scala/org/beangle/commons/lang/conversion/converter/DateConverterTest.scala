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
package org.beangle.commons.lang.conversion.converter

import org.testng.Assert.assertEquals
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.beangle.commons.lang.conversion.Converter
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

class DateConverterTest {

  @Test
  def testConvertoDate() {
    var date1 = "19800909"
    converToDate(date1, 1980, 8, 9)
    date1 = "1980-09-09"
    converToDate(date1, 1980, 8, 9)
  }

  private def converToDate(dateStr: String, 
      year: Int, 
      month: Int, 
      day: Int) {
    val c = new String2DateConverter().getConverter(classOf[Date])
    val date = c.apply(dateStr)
    val calendar = new GregorianCalendar()
    calendar.setTime(date)
    assertEquals(calendar.get(Calendar.YEAR), year)
    assertEquals(calendar.get(Calendar.MONTH), month)
    assertEquals(calendar.get(Calendar.DAY_OF_MONTH), day)
  }

  def testNormalize() {
    val converter= new String2DateConverter();
    assertEquals("1980-09-01", converter.normalize("1980-9-1"))
    assertEquals("1980-09-01", converter.normalize("1980-09-1"))
    assertEquals("1980-09-01", converter.normalize("1980-9-01"))
    assertEquals("1980-09-01", converter.normalize("1980-09-01"))
  }
}
