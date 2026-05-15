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

package org.beangle.commons.lang

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.*
import java.time.format.DateTimeFormatter

class DatesTest extends AnyFunSpec, Matchers {

  describe("Dates") {
    it("join") {
      val date = LocalDate.parse("2014-09-09")
      val time = LocalTime.parse("09:09:10")
      val datetime = LocalDateTime.of(date, time)
      datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) should equal("2014-09-09 09-09-10")
    }

    it("Normalize date string") {
      Dates.normalizeDate("1980/09/01") should equal("1980-09-01")
      Dates.normalizeDate("19800901") should equal("1980-09-01")
      Dates.normalizeDate("1980-9-1") should equal("1980-09-01")
      Dates.normalizeDate("1980-09-1") should equal("1980-09-01")
      Dates.normalizeDate("1980-9-01") should equal("1980-09-01")
      Dates.normalizeDate("1980-09-01") should equal("1980-09-01")
      Dates.normalizeDate("1980.9.1") should equal("1980-09-01")
    }
    it("convert string to Instant") {
      val i = Dates.toDateTime("1980-09-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant
      assert(i == Dates.toInstant("1980.9.1 00:00"))
      assert(i == Dates.toInstant("1980-9-1 00:00"))
      assert(i == Dates.toInstant("1980.9.1"))
      assert(i == Dates.toInstant("1980.9.1T"))
      assert(i == Dates.toInstant("1980.9.1 00"))
    }

    it("convert string to YearMonth") {
      Dates.toYearMonth("2023/09/01") should equal(YearMonth.parse("2023-09"))
      Dates.toYearMonth("2023/9") should equal(YearMonth.parse("2023-09"))
      Dates.toYearMonth("20230901") should equal(YearMonth.parse("2023-09"))
      // '-' 分隔且包含日部分的日期也应归一化为年月
      Dates.toYearMonth("2023-09-15") should equal(YearMonth.parse("2023-09"))
    }

    it("convert string to LocalDateTime") {
      Dates.toDateTime("2023/09/01 9:02:03") should equal(LocalDateTime.parse("2023-09-01T09:02:03"))
      Dates.toDateTime("2023-09-01 9:02:03") should equal(LocalDateTime.parse("2023-09-01T09:02:03"))
    }
  }
}
