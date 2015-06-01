/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
import org.beangle.commons.lang.time.WeekDay._
import java.{ util => ju }

@RunWith(classOf[JUnitRunner])
class WeekDayTest extends FunSpec with Matchers {

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
      val now = ju.Calendar.getInstance
      //2015-4-8
      now.set(2015, 3, 8)
      assert(WeekDay.of(now.getTime) == Wed)
    }
  }
}