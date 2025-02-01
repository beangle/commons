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

class IdentitySetTest extends AnyFunSpec, Matchers {

  describe("IdentitySet") {
    it("put and get") {
      val set = new IdentitySet[List[Int]]
      val list1 = List(1, 2)
      val list2 = List(1, 2)
      assert(!(list1 eq list2))
      set += list1
      assert(set.contains(list1))
      assert(!set.contains(list2))
      set += list2
      assert(set.size == 2)
    }

    it("iterate elememts") {
      val set = new IdentitySet[Integer]
      set += 2
      set += 4
      val itor = set.iterator
      assert(itor.hasNext)
      assert(null != itor.next())
      assert(itor.hasNext)
      assert(itor.hasNext)
      assert(null != itor.next())
    }
  }
}
