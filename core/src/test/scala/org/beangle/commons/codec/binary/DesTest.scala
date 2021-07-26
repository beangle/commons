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

package org.beangle.commons.codec.binary

import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

class DesTest extends AnyFunSpec with Matchers {
  describe("Des cbc") {
    it("encode and decode") {
      var key = "ABCDEFGH"
      val value = "AABBCCDDEE"
      val encrypted = Des.CBC.encode(key, value.getBytes)
      assert("c3ed812241678c3877561d25f9b3ac4e" == Hex.encode(encrypted))
      assert(value == Des.CBC.decodeHex(key, "c3ed812241678c3877561d25f9b3ac4e"))
    }
  }
}
