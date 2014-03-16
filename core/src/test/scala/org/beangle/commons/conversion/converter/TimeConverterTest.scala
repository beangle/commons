/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.beangle.commons.conversion.Converter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.sql.Time

@RunWith(classOf[JUnitRunner])
class TimeConverterTest extends FunSpec with Matchers {

  describe("TimeConverter") {
    it("Convert String to time") {
      String2TimeConverter("1234") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("123400") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("12:34") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("12:34:00") should equal(Time.valueOf("12:34:00"))
    }
  }
}