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

package org.beangle.commons.json

import org.beangle.commons.lang.math.{Decimal5, TinyDecimal5}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.math.{BigDecimal as JBigDecimal, BigInteger}

/** 校验 [[Json.toLiteral]] 的数值字面量策略。
 *
 * - `TinyDecimal5`/`Decimal5` 输出 JSON number，用 `toBigDecimal.toPlainString`（无科学计数法）；
 * - 其余 `Number`/`BigDecimal`/`BigInteger` 仍输出字符串，避免精度不可控；
 * - 既有行为保护：String/Boolean/null 不变，Long 仍输出带引号字符串（避免 JS 大整数精度丢失）。
 */
class JsonNumberLiteralTest extends AnyFunSpec, Matchers {

  describe("Json.toLiteral") {
    it("emits JSON numbers for fixed-scale decimals") {
      Json.toLiteral(TinyDecimal5.of("1.5")) should equal("1.5")
      Json.toLiteral(Decimal5.of("1.5")) should equal("1.5")
      Json.toLiteral(TinyDecimal5.of("-2.25")) should equal("-2.25")
      Json.toLiteral(TinyDecimal5.of("2")) should equal("2")
    }

    it("keeps other numbers as strings to avoid precision loss") {
      Json.toLiteral(new JBigDecimal("1.5")) should equal("\"1.5\"")
      Json.toLiteral(BigDecimal("1.5")) should equal("\"1.5\"")
      Json.toLiteral(new BigInteger("12345678901234567890")) should equal("\"12345678901234567890\"")
    }

    it("keeps primitives and strings unchanged") {
      Json.toLiteral(42) should equal("42")
      Json.toLiteral(1.5d) should equal("1.5")
      Json.toLiteral(1.5f) should equal("1.5")
      Json.toLiteral("x") should equal("\"x\"")
      Json.toLiteral(true) should equal("true")
      Json.toLiteral(null) should equal("null")
      // 既有兼容行为：Long 仍带引号
      Json.toLiteral(42L) should equal("\"42\"")
    }

    it("serializes fixed-scale decimals inside JsonObject without quotes") {
      val json = JsonObject("credits" -> TinyDecimal5.of("1.5"), "amount" -> BigDecimal("0.1"))
      json.toJson should include("\"credits\":1.5")
      json.toJson should include("\"amount\":\"0.1\"")
    }
  }
}
