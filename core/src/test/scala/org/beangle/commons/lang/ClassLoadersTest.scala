package org.beangle.commons.lang

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ClassLoadersTest extends FunSpec with Matchers {

  describe("ClassLoaders") {
    it("newInstance") {
      val a:String = ClassLoaders.newInstance("java.lang.String")
      assert(a.length == 0)
    }
  }
}