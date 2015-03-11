package org.beangle.commons.collection

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IdentityMapTest extends FunSpec with Matchers {

  describe("IdentityMap") {
    it("put") {
      val cache = new IdentityMap[String, String]
      cache.put("cn", "CHINA")
      cache.put("us", "USA")
      assert("CHINA" == cache.get("cn"))
      cache.get("us")
    }
  }

}