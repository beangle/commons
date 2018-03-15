/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat

@RunWith(classOf[JUnitRunner])
class DatesTest extends FunSpec with Matchers {

  describe("Dates") {
    it("join") {
      val date = Date.valueOf("2014-09-09")
      val time = Time.valueOf("09:09:10")
      val datetime = Dates.join(date, time)
      val format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
      format.format(datetime) should equal("2014-09-09 09-09-10")
    }

    it("Normalize date string") {
      Dates.normalize("1980-9-1") should equal("1980-09-01")
      Dates.normalize("1980-09-1") should equal("1980-09-01")
      Dates.normalize("1980-9-01") should equal("1980-09-01")
      Dates.normalize("1980-09-01") should equal("1980-09-01")
      Dates.normalize("1980.9.1") should equal("1980-09-01")
    }
  }
}
