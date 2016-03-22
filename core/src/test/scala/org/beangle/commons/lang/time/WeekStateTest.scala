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
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WeekStateTest extends FunSpec with Matchers {

  describe("WeekState") {
    it("toString ") {
      assert(new WeekState(9).toString == "1001")
      assert(new WeekState(12).toString == "1100")
    }
    it("apply string") {
      assert(WeekState("1100").value == 12)
      assert(WeekState("1100").first == 2)
      assert(WeekState("1100").last == 3)
    }

    it("get span") {
      assert(WeekState("1100").span == (2 -> 3))
    }

    it("get weeks") {
      assert(WeekState("10110").weeks == 3)
      assert(WeekState("").weeks == 0)
      assert(WeekState("").first == -1)
      assert(WeekState("").last == -1)
    }

    it("get weekList") {
      assert(WeekState("101100").weekList == List(2, 3, 5))
    }

    it("is occupied") {
      assert(WeekState("101100").isOccupied(2))
      assert(!WeekState("101100").isOccupied(4))
    }

    it("is serializable") {
      assert(WeekState("101100").isInstanceOf[Serializable])
    }

    it("equals") {
      assert(WeekState("101100") == new WeekState(44))
    }
    it("hashCode") {
      assert(WeekState("101100").hashCode == java.lang.Long.hashCode(44L))
    }
  }
}
