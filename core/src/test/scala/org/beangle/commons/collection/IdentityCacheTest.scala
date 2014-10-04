package org.beangle.commons.collection

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IdentityCacheTest extends FunSpec with Matchers {

  describe("IdentityCache") {
    it("put") {
      val cache = new IdentityCache[String, String]
      cache.put("cn", "CHINA")
      cache.put("us", "USA")
      assert("CHINA" == cache.get("cn"))
      cache.get("us")
    }
  }

}