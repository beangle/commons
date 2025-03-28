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

package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.testbean.Dog
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class BridgeMethodTest extends AnyFunSpec, Matchers {

  describe("Reflection class bridge method") {
    it("Find bridge methods") {
      for (m <- classOf[Dog].getMethods if m.getName == "getAge")
        if (m.getReturnType == classOf[Number])
          assert(m.isBridge)
    }
  }
}
