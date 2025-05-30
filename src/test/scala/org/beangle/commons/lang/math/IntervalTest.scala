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

package org.beangle.commons.lang.math

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class IntervalTest extends AnyFunSpec, Matchers {
  describe("Interval") {
    it("small") {
      val i = SmallInterval(3, 4)
      i.value should be(196612)
      i.begin should be(3)
      i.end should be(4)
      i.toString should equal("3-4")
    }
    it("normal") {
      val i = Interval(3, 4)
      i.value should be(12884901892L)
      i.begin should be(3)
      i.end should be(4)
      i.toString should equal("3-4")
    }
  }
}
