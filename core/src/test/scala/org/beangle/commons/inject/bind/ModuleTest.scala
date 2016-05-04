package org.beangle.commons.inject.bind

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModuleTest extends FunSpec with Matchers {

  describe("BindModule") {
    it("has corrent dev model detected.") {
      val module = new AbstractBindModule {
        protected override def binding(): Unit = {}
      }
      System.setProperty(Module.profileProperty, "dev")
      assert(module.devEnabled)
      System.clearProperty(Module.profileProperty)
      assert(!module.devEnabled)
      System.setProperty(Module.profileProperty, "dev,other")
      assert(module.devEnabled)
    }
  }
}
