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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class Base64Test extends AnyFunSpec, Matchers {
  describe("Base64") {
    it("encode and decode") {
      val data = "xtUkXCyc+OAdHqK1lJKubmnzGts="
      val bytes = Base64.decode(data)
      val newData = Base64.encode(bytes)

      assert(data == newData)
    }
  }
}
