/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter
import org.scalatest.FunSpec
import org.scalatest.Matchers
import java.lang.reflect.InvocationTargetException
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.testbean.TestEnum
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author chaostone
 * @since 3.0.0
 */
@RunWith(classOf[JUnitRunner])
class EnumConverterTest extends FunSpec with Matchers {

  describe("EnumConverter") {
    it("Convert Enum") {
      DefaultConversion.Instance.convert("Private", classOf[TestEnum.Val])
    }
  }
}
