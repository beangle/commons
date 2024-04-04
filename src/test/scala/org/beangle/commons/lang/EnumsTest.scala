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

import org.beangle.commons.conversion.string.EnumConverters
import org.beangle.commons.lang.time.WeekDay
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class EnumsTest extends AnyFunSpec with Matchers {

  describe("Enums") {
    it("get") {
      assert(EnumConverters.convert("7", classOf[WeekDay]) == WeekDay.Sun)
      assert(Enums.get(classOf[WeekDay], "Sun").contains(WeekDay.Sun))
      assert(Enums.isEnum(classOf[WeekDay]))
      assert(Enums.of(classOf[WeekDay], 1).contains(WeekDay.Mon))

      assert(Enums.id(WeekDay.Thu) == 4)
    }
  }

}
