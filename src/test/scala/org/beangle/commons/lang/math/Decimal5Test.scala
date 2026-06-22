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

import org.beangle.commons.conversion.converter.Number2NumberConverter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.math.BigDecimal

class Decimal5Test extends AnyFunSpec, Matchers {

  describe("Decimal5") {
    it("stores scaled long value") {
      val d = Decimal5.of("12.34567")
      d.value should be(1234567L)
      d.toString should equal("12.34567")
    }

    it("rounds with HALF_UP") {
      Decimal5.of("1.234565").toString should equal("1.23457")
    }

    it("constructs from double without binary noise") {
      Decimal5.of(0.1 + 0.2).toString should equal("0.3")
    }

    it("supports string round trip and blank as zero") {
      val d = Decimal5.of(" -0.00001 ")
      d.toString should equal("-0.00001")
      Decimal5.of("").value should be(0L)
    }

    it("adds and subtracts with exact") {
      val a = Decimal5.of("1")
      val b = Decimal5.of("0.00001")
      (a + b).toString should equal("1.00001")
      (a - b).toString should equal("0.99999")
    }

    it("exposes Number semantics") {
      val d = Decimal5.of("12.34567")
      d.longValue() should be(12L)
      d.doubleValue() should be(12.34567 +- 0.000001)
    }

    it("converts to BigDecimal exactly") {
      Decimal5.of("12.34567").toBigDecimal should equal(new BigDecimal("12.34567"))
    }

    it("converts from TinyDecimal5 losslessly") {
      val tiny = TinyDecimal5.of("3.14159")
      Decimal5.of(tiny).toString should equal("3.14159")
    }

    it("rejects Decimal5 to TinyDecimal5 when out of int range") {
      val large = Decimal5.of("100000")
      intercept[ArithmeticException] {
        large.toTinyDecimal5
      }
    }

    it("converts via Number2NumberConverter to BigDecimal") {
      val d = Decimal5.of("9.87654")
      Number2NumberConverter.convert(d, classOf[BigDecimal]) should equal(new BigDecimal("9.87654"))
    }

    it("strips trailing zeros in toString") {
      Decimal5.of("12").toString should equal("12")
      Decimal5.Zero.toString should equal("0")
      Decimal5.of("12.34000").toString should equal("12.34")
    }

    it("has min and max boundaries") {
      Decimal5.MinValue.toString should equal("-92233720368547.75808")
      Decimal5.MaxValue.toString should equal("92233720368547.75807")
    }
  }
}

