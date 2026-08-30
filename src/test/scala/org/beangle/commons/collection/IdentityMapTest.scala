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

package org.beangle.commons.collection

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class IdentityMapTest extends AnyFunSpec, Matchers {

  describe("IdentityMap") {
    it("put") {
      val cache = new IdentityMap[String, String]
      cache.put("cn", "CHINA")
      cache.put("us", "USA")
      assert("CHINA" == cache.get("cn"))
      cache.get("us")
    }

    it("grow and keep entries") {
      val map = new IdentityMap[String, String]
      val keys = (0 until 2000).map(i => s"k$i")
      keys.foreach(k => map.put(k, "v"))
      assert(map.size() == 2000)
      keys.foreach(k => assert(map.get(k) == "v"))
    }

    it("remove after grow") {
      val map = new IdentityMap[String, String]
      val keys = (0 until 100).map(i => s"k$i")
      keys.foreach(k => map.put(k, "v"))
      keys.take(50).foreach(k => map.remove(k))
      assert(map.size() == 50)
      keys.drop(50).foreach(k => assert(map.get(k) == "v"))
      keys.take(50).foreach(k => assert(!map.contains(k)))
    }

    it("clear resets size") {
      val map = new IdentityMap[String, String]
      (0 until 100).foreach(i => map.put(s"k$i", "v"))
      map.clear()
      assert(map.size() == 0)
      assert(map.get("k0") == null)
    }
  }
}
