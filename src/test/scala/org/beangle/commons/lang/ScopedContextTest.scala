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

class ScopedContextTest extends AnyFunSpec, Matchers {

  describe("ScopedContext") {
    it("rebind") {
      val k1 = ScopedContext.Key[Long]("k1")
      val k2 = ScopedContext.Key[Long]("k2")
      assert(ScopedContext.get(k1).isEmpty)
      ScopedContext.runWith(k1 -> 3L, k2 -> 100) {
        assert(ScopedContext.get(k1).contains(3L))
        assert(ScopedContext.get(k2).contains(100))
      }
    }
  }

}
