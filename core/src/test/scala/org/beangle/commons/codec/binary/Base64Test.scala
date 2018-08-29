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
package org.beangle.commons.codec.binary

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Base64Test extends FunSpec with Matchers {
  describe("Base64") {
    it("encode and decode") {
      val data = "xtUkXCyc+OAdHqK1lJKubmnzGts="
      val bytes = Base64.decode(data)
      val newData = Base64.encode(bytes)
      val bb=java.util.Base64.getDecoder.decode(data)
      val newbb=java.util.Base64.getEncoder.encodeToString(bb);

      assert(data == newData)
    }
  }
}
