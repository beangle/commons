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

package org.beangle.commons

import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Numbers.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class NumbersTest extends AnyFunSpec, Matchers {

  describe("Numbers") {
    it("isDigits") {
      assert(isDigits("23"))
      assert(isDigits("-23"))

      println(round(4.0153d,3))
      assert(round(4.015d, 2).toString == "4.02") //3.14
      assert(round(add(0.05, 0.01), 2).toString == "0.06")
      assert(round(subtract(1.0, 0.42), 2).toString == "0.58")
      assert(round(multiply(4.015, 100), 3).toString == "401.5")
    }
  }

}
