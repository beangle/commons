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
package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HourMinuteTest extends FunSpec with Matchers {

  describe("HourMinute") {
    it("is serializable") {
      assert(HourMinute("12:34").isInstanceOf[Serializable])
    }
    it("distance") {
      assert(60 == (HourMinute("13:34") - HourMinute("12:34")))
    }
    it("equals") {
      assert(HourMinute("12:34") == new HourMinute(1234))
    }

    it("plus") {
      assert(HourMinute("12:34") + 30 == new HourMinute(1304))
      assert(HourMinute("12:34") - 30 == new HourMinute(1204))
      assert(HourMinute("12:34") + 120 == new HourMinute(1434))
      assert(HourMinute("23:34") + 110 == new HourMinute(124))
      assert(HourMinute("01:34") - 110 == new HourMinute(2344))
    }
  }
}
