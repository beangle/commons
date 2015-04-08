/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.conversion.converter

import org.scalatest.FunSpec
import org.scalatest.Matchers

import java.{util => ju}
import org.beangle.commons.conversion.Converter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DateConverterTest extends FunSpec with Matchers {

  private def converToDate(dateStr: String,
    year: Int,
    month: Int,
    day: Int) {
    val c = new String2DateConverter().getConverter(classOf[ju.Date]).orNull
    val date = c.apply(dateStr)
    val calendar = new ju.GregorianCalendar()
    calendar.setTime(date)
    calendar.get(ju.Calendar.YEAR) should be(year)
    calendar.get(ju.Calendar.MONTH) should be(month)
    calendar.get(ju.Calendar.DAY_OF_MONTH) should be(day)
  }

  describe("DateConverter") {
    it("Convert String to date") {
      converToDate("19800909", 1980, 8, 9)
      converToDate("1980-09-09", 1980, 8, 9)
    }

    it("Normalize date string") {
      val converter = new String2DateConverter();
      converter.normalize("1980-9-1") should equal("1980-09-01")
      converter.normalize("1980-09-1") should equal("1980-09-01")
      converter.normalize("1980-9-01") should equal("1980-09-01")
      converter.normalize("1980-09-01") should equal("1980-09-01")
    }
  }
}
