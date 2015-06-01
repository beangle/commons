package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import java.sql.Date

@RunWith(classOf[JUnitRunner])
class WeekTimeTest extends FunSpec with Matchers {
  describe("WeekTime") {
    it("firstDate ") {
      val wt = new WeekTime
      wt.startOn = Date.valueOf("2014-12-28")
      wt.weekstate = WeekState("101");
      wt.weekday = WeekDay.Thu
      assert(wt.firstDate == Date.valueOf("2015-01-01"))
      wt.weekday = WeekDay.Fri
      wt.weekstate = WeekState("110");
      assert(wt.firstDate == Date.valueOf("2015-01-09"))
    }
  }
}