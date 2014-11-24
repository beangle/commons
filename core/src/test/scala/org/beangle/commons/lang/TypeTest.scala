package org.beangle.commons.lang

import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class TypeTest extends FunSpec with Matchers {

  describe("Types") {
    it("java primitives") {
      val a = new JDouble(0)
      println(a.getClass)
    }
  }
}