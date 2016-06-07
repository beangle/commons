/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import java.sql.Date

@RunWith(classOf[JUnitRunner])
class WeekTimeTest extends FunSpec with Matchers {
  describe("WeekTime") {
    it("firstDate ") {
      val wt = new WeekTime
      wt.startOn = Date.valueOf("2014-12-28")
      wt.weekstate = WeekState("1010");
      wt.weekday = WeekDay.Thu
      assert(wt.firstDate == Date.valueOf("2015-01-01"))
      wt.weekday = WeekDay.Fri
      wt.weekstate = WeekState("1100");
      assert(wt.firstDate == Date.valueOf("2015-01-09"))
    }
  }
}