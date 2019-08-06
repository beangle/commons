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

import java.time.LocalDate

import org.beangle.commons.lang.time.WeekDay.{ Mon, Sat, Sun, Wed }
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WeekDayTest extends AnyFunSpec with Matchers {

  describe("WeekDay") {
    it("id starts at Mon") {
      assert(Mon.id == 1)
      assert(Sun.id == 7)
      assert(Sat.id == 6)
    }

    it("is serializable") {
      assert(Sun.isInstanceOf[Serializable])
    }

    it("index starts at Sun") {
      assert(Sun.index == 1)
      assert(Mon.index == 2)
      assert(Sat.index == 7)
    }

    it("of some day") {
      //2015-4-8
      val now = LocalDate.of(2015, 4, 8)
      assert(WeekDay.of(now) == Wed)
    }
  }
}
