package org.beangle.commons.lang.time

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.time.WeekDays._

@RunWith(classOf[JUnitRunner])
class WeekDayTest extends FunSpec with Matchers {

  describe("WeekDay") {
    it("id starts at Mon") {
      assert(Mon.id == 1)
      assert(Sun.id == 7)
      assert(Sat.id == 6)
    }

    it("is serializable") {
      assert(Sun.isInstanceOf[Serializable])
    }

    it("index starts at Sun") {
      assert(Sun.index == 1)
      assert(Mon.index == 2)
      assert(Sat.index == 7)
    }
  }
}