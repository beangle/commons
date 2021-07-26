/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.conversion.converter

import java.sql.Time

import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

import org.beangle.commons.lang.time.HourMinute

class TimeConverterTest extends AnyFunSpec with Matchers {

  describe("TimeConverter") {
    it("Convert String to time") {
      String2TimeConverter("1234") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("123400") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("090619") should equal(Time.valueOf("09:06:19"))
      String2TimeConverter("12:34") should equal(Time.valueOf("12:34:00"))
      String2TimeConverter("12:34:00") should equal(Time.valueOf("12:34:00"))

      String2HourMinuteConverter("12:34") should equal(new HourMinute(1234.toShort))
    }
  }
}
