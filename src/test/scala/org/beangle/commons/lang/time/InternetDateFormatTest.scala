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

package org.beangle.commons.lang.time

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util as ju

class InternetDateFormatTest extends AnyFunSpec, Matchers {
  describe("InternetDateFormat") {
    it("format") {
      val cal = ju.Calendar.getInstance
      cal.set(2030, 9 - 1, 1, 9, 10, 1)
      cal.set(ju.Calendar.MILLISECOND, 0)
      val date = cal.getTime
      val utcString = DateFormats.UTC.format(date)
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      val newDate = format.format(date)
      assert(utcString == newDate)

      val gmtString = DateFormats.GMT.format(date)
      assert(gmtString == "2030-09-01T01:10:01.000Z")
    }
    it("parse") {
      val s = "2014-12-17T04:37:15.230Z"
      val date1 = DateFormats.UTC.parse("2014-12-17T04:37:15.230Z")
      val date2 = DateFormats.UTC.parse("2014-12-17 04:37:15.230Z")
      assert(date1 == date2)
    }
  }
}
