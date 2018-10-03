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
import java.util.Arrays

@RunWith(classOf[JUnitRunner])
class AesTest extends FunSpec with Matchers {
  describe("Aes cbc") {
    it("encode and decode") {
      var key = "8NONwyJtHesysWpM"
      val value = "ABCDEFGH"
      val encrypted = Aes.ECB.encode(key, value.getBytes())
      val a = Hex.decode("3c2b1416d82883dfeaa6a9aa5ecb8245")
      assert(Arrays.equals(a, encrypted))
      assert("3c2b1416d82883dfeaa6a9aa5ecb8245" == Hex.encode(encrypted))
      val d = Aes.ECB.decode(key, a)
      assert(value == new String(Aes.ECB.decode(key, a)))
    }
  }
}
