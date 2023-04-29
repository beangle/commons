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

package org.beangle.commons.lang.text

import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.text.Formatters
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.{Instant, LocalDate}
import java.util.Date

class FormatsTest extends AnyFunSpec with Matchers {
  describe("Formats") {
    it("format number") {
      Formatters.format(123.121) should equal("123.12")
      Formatters.format(123.125d, "#,##0.##") should equal("123.13")
      Formatters.format(122323321) should equal("122323321")
      Formatters.format(122323321L) should equal("122,323,321")
    }
    it("format java.util.*") {
      val date = new Date(2030-1900,9-1,1,9,10,1)
      val c = java.util.Calendar.getInstance()
      c.setTime(date)

      Formatters.format(new java.sql.Date(date.getTime)) should equal("2030-09-01")
      Formatters.format(java.sql.Timestamp.from(date.toInstant)) should equal("2030-09-01 09:10:01")
      Formatters.format(java.sql.Time.valueOf("09:10:01")) should equal("09:10:01")

      Formatters.format(date) should equal("2030-09-01 09:10:01")
      Formatters.format(c) should equal("2030-09-01 09:10:01")
    }

    it("format java.time.*") {
      println(Formatters.format(Instant.now))
      Formatters.format(LocalDate.parse("2023-04-16")) should equal("2023-04-16")
      Formatters.format(java.sql.Date.valueOf("2030-09-01")) should equal("2030-09-01")
    }
  }
}
