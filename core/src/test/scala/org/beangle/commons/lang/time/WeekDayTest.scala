package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WeekDayTest extends FunSpec with Matchers {

  describe("WeekDay") {
    it("value start 0") {
      assert(WeekDay.Sun.id == 7)
      assert(WeekDay.Mon.id == 1)
      assert(WeekDay.Sat.id == 6)
    }
  }
}