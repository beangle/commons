package org.beangle.commons.cache.caffeine

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CaffeineCacheTest extends FunSpec with Matchers {

  describe("CaffeineCache") {
    it("Get or Put") {
      val manager = new CaffeineCacheManager(true)
      val cache = manager.getCache("test", classOf[Long], classOf[String])
      cache.put(1L, "beijing")
      cache.put(2L, "shanghai")
      cache.get(1) should be equals ("beijing")

      assert(cache.get(3) == None)
    }

    it("get cache") {
      val manager = new CaffeineCacheManager(false)
      val cache = manager.getCache("test", classOf[Long], classOf[String])
      assert(null != cache)
      val cache2 = manager.getCache("test1", classOf[Long], classOf[String])
      assert(null == cache2)
    }
  }
}
