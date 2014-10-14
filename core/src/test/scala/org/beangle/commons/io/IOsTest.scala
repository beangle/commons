package org.beangle.commons.io
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.ClassLoaders

@RunWith(classOf[JUnitRunner])
class IOsTest extends FunSpec with Matchers {
  describe("IOs") {
    it("readBundles") {
      val url = ClassLoaders.getResource("message2.zh_CN")
      val bundles = IOs.readBundles(url.openStream)
      assert(null != bundles)
      assert(bundles.contains(""))
      val thisMap = bundles("")
      assert(thisMap.size == 1)
      assert(bundles.contains("Country"))
      val countryMap = bundles("Country")
      assert(countryMap.size == 1)

    }
  }
}