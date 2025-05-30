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

class IntSegTest extends AnyFunSpec, Matchers {
  describe("IntSeg") {
    it("digest") {
      val nums = IntSeg.parse("7~9单，10-15，2-7双,18-17,19,30")
      val expected = Set(2, 4, 6, 7, 9, 10, 11, 12, 13, 14, 15, 19, 30)
      assert(expected == nums.toSet)
      assert("2~6双 7~9单 10~15 19 30" == IntSeg.digest(nums, "~", " "))

      assert(IntSeg.parse("").isEmpty)
    }
  }
}
