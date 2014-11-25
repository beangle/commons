package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HourMinuteTest extends FunSpec with Matchers {

  describe("HourMinute") {
    it("is serializable") {
      assert(HourMinute(1234.asInstanceOf[Short]).isInstanceOf[Serializable])
    }
  }
}