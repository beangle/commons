/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.lang

import org.beangle.commons.lang.Doubles.{equals as doubleEquals, *}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DoublesTest extends AnyFunSpec, Matchers {

  describe("Doubles") {

    describe("round") {
      it("rounds with HALF_UP using BigDecimal") {
        round(4.015d, 2).toString should be("4.02")
        round(2.675d, 2).toString should be("2.68")
        round(1.234d, 1).toString should be("1.2")
        round(1.235d, 2).toString should be("1.24")
      }

      it("supports negative values and zero scale") {
        round(-1.235d, 2).toString should be("-1.24")
        round(3.5d, 0).toString should be("4.0")
        round(2.4d, 0).toString should be("2.0")
      }
    }

    describe("add") {
      it("avoids binary floating-point accumulation error") {
        add(0.05, 0.01) should be(0.06)
        add(0.1, 0.2) should be(0.3)
        round(add(0.05, 0.01), 2).toString should be("0.06")
      }
    }

    describe("subtract") {
      it("avoids binary floating-point subtraction error") {
        subtract(1.0, 0.42) should be(0.58)
        round(subtract(1.0, 0.42), 2).toString should be("0.58")
        subtract(0.3, 0.1) should be(0.2)
      }
    }

    describe("multiply") {
      it("avoids binary floating-point multiplication error") {
        multiply(4.015, 100) should be(401.5)
        round(multiply(4.015, 100), 3).toString should be("401.5")
        multiply(0.1, 3) should be(0.3)
      }
    }

    describe("divide") {
      it("divides with HALF_UP and default scale") {
        divide(1.0, 3.0) should be(0.3333333333 +- 1e-10)
        divide(10.0, 4.0) should be(2.5)
      }

      it("respects custom scale") {
        divide(1.0, 3.0, 2).toString should be("0.33")
        divide(2.0, 3.0, 4).toString should be("0.6667")
        divide(1.0, 8.0, 3).toString should be("0.125")
      }
    }

    describe("isZero") {
      it("treats zero and tiny values within default epsilon as zero") {
        isZero(0.0) should be(true)
        isZero(-0.0) should be(true)
        isZero(1e-7) should be(true)
        isZero(-1e-7) should be(true)
      }

      it("rejects values outside default epsilon") {
        isZero(1e-5) should be(false)
        isZero(0.1) should be(false)
        isZero(Double.NaN) should be(false)
      }
    }

    describe("compare") {
      it("returns 0 when values are equal within epsilon") {
        compare(1.0, 1.0000001, 0.001) should be(0)
        compare(0.0, -0.0, 1e-6) should be(0)
      }

      it("returns -1 or 1 when values differ beyond epsilon") {
        compare(1.0, 2.0, 0.001) should be(-1)
        compare(2.0, 1.0, 0.001) should be(1)
        compare(1.0, 1.01, 0.001) should be(-1)
      }
    }

    describe("equals with epsilon") {
      it("matches when ULP distance is within limit or absolute diff <= epsilon") {
       doubleEquals(1.0, 1.0, 0.001) should be(true)
       doubleEquals(0.0, -0.0, 0.001) should be(true)
       doubleEquals(1.0, 1.0005, 0.001) should be(true)
      }

      it("rejects NaN and large differences") {
       doubleEquals(Double.NaN, Double.NaN, 0.001) should be(false)
       doubleEquals(1.0, Double.NaN, 0.001) should be(false)
       doubleEquals(1.0, 2.0, 0.001) should be(false)
      }

      it("treats same-signed infinities as equal") {
       doubleEquals(Double.PositiveInfinity, Double.PositiveInfinity, 0.001) should be(true)
       doubleEquals(Double.NegativeInfinity, Double.NegativeInfinity, 0.001) should be(true)
       doubleEquals(Double.PositiveInfinity, Double.NegativeInfinity, 0.001) should be(false)
      }
    }

    describe("equals without epsilon") {
      it("uses ULP distance of 1") {
       doubleEquals(1.0, 1.0) should be(true)
       doubleEquals(0.0, -0.0) should be(true)
       doubleEquals(1.0, 1.0000000000000002) should be(true)
      }

      it("rejects NaN and distant values") {
       doubleEquals(Double.NaN, Double.NaN) should be(false)
       doubleEquals(1.0, 1.1) should be(false)
      }
    }

    describe("ulpEquals") {
      it("treats +0.0 and -0.0 as equal") {
        ulpEquals(0.0, -0.0, 0) should be(true)
        ulpEquals(0.0, -0.0, 1) should be(true)
      }

      it("compares adjacent representable values within maxUlps") {
        val x = 1.0
        val next = Math.nextUp(x)
        ulpEquals(x, next, 1) should be(true)
        ulpEquals(x, next, 0) should be(false)
      }

      it("handles infinities and NaN") {
        ulpEquals(Double.PositiveInfinity, Double.PositiveInfinity, 1) should be(true)
        ulpEquals(Double.PositiveInfinity, Double.NegativeInfinity, 1) should be(false)
        ulpEquals(Double.NaN, Double.NaN, 1) should be(false)
        ulpEquals(1.0, Double.NaN, 1) should be(false)
      }
    }
  }

}
