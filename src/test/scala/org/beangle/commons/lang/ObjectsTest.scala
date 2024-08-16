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

package org.beangle.commons.lang

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ObjectsTest extends AnyFunSpec with Matchers {

  describe("Objects") {
    it("Equals object and array") {
      Objects.equals(null, null) should be(true)
      // so strange
      Objects.equals(Array(2), Array(2)) should be(false)
    }
    it("Compare object and array") {
      Objects.compareBuilder.add(1, 2).toComparison should be(-1)
    }
    it("provider default value") {
      Objects.nvl(null, "2") should equal("2")
      Objects.nvl("3", "2") should equal("3")
      Objects.nvl(null, null) should equal(null)
      //test lazy evaluate
      Objects.nvl("33", if (3 > 0) throw RuntimeException("should not be throw") else "44") should equal("33")
    }
  }
}
