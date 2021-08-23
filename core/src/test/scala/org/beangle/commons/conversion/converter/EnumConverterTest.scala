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

package org.beangle.commons.conversion.converter
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.testbean.TestEnum
import org.beangle.commons.lang.time.WeekDay

import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

/**
 * @author chaostone
 * @since 3.0.0
 */

class EnumConverterTest extends AnyFunSpec with Matchers {

  describe("EnumConverter") {
    it("Convert Enum") {
      println(TestEnum.Private.getClass)
      assert(null != DefaultConversion.Instance.convert("Private", classOf[TestEnum]))
      assert(null != DefaultConversion.Instance.convert("Sun", classOf[WeekDay]))
      assert(WeekDay.Sat == DefaultConversion.Instance.convert("6", classOf[WeekDay]))
    }
  }
}
