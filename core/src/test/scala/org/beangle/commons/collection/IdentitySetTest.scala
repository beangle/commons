package org.beangle.commons.collection

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IdentitySetTest extends FunSpec with Matchers {

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