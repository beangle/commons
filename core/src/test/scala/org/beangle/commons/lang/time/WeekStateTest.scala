package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WeekStateTest extends FunSpec with Matchers {

  describe("WeekState") {
    it("toString ") {
      assert(new WeekState(9).toString == "1001")
      assert(new WeekState(12).toString == "1100")
    }
    it("apply string") {
      assert(WeekState("1100").value == 12)
    }

    it("get span") {
      assert(WeekState("1100").span == (2 -> 3))
    }

    it("get weeks") {
      assert(WeekState("101100").weeks == 3)
    }

    it("get weekList") {
      assert(WeekState("101100").weekList == List(2, 3, 5))
    }

    it("is occupied") {
      assert(WeekState("101100").isOccupied(2))
      assert(!WeekState("101100").isOccupied(4))
    }
  }
}