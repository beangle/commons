package org.beangle.commons.lang

import org.beangle.commons.net.Networks
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class SystemInfoTest extends AnyFunSpec, Matchers {

  describe("SystemInfo") {
    it("user properties") {
      assert(null != SystemInfo.user.dir)
      assert(null != SystemInfo.user.home)
      assert(Networks.ipv6.contains("::1"))
    }
  }
}
