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

package org.beangle.commons.lang.math

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.math.BigDecimal

class TinyDecimal5Test extends AnyFunSpec, Matchers {

  describe("TinyDecimal5") {
    it("stores scaled int value") {
      val d = TinyDecimal5.of("12.34567")
      d.value should be(1234567)
      d.toString should equal("12.34567")
    }

    it("constructs from BigDecimal") {
      TinyDecimal5.of(new BigDecimal("-3.00050")).toString should equal("-3.0005")
    }

    it("adds and subtracts") {
      val a = TinyDecimal5.of("1")
      val b = TinyDecimal5.of("0.00001")
      (a + b).toString should equal("1.00001")
    }

    it("converts to Decimal5") {
      TinyDecimal5.of("2.5").toDecimal5.toString should equal("2.5")
    }

    it("has min and max boundaries") {
      TinyDecimal5.MinValue.toString should equal("-21474.83648")
      TinyDecimal5.MaxValue.toString should equal("21474.83647")
    }

    it("throws on overflow when adding") {
      val max = TinyDecimal5.MaxValue
      intercept[ArithmeticException] {
        max + TinyDecimal5.of("0.00001")
      }
    }

    it("converts to BigDecimal exactly") {
      TinyDecimal5.of("0.00123").toBigDecimal should equal(new BigDecimal("0.00123"))
    }
  }
}
