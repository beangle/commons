package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.time.WeekDays._

@RunWith(classOf[JUnitRunner])
class WeekDayTest extends FunSpec with Matchers {

  describe("WeekDay") {
    it("value start 0") {
      assert(Sun.id == 7)
      assert(Mon.id == 1)
      assert(Sat.id == 6)
    }
  }
}