/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter

import org.beangle.commons.conversion.impl.DefaultConversion
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefaultConverterTest extends AnyFunSpec with Matchers {

  describe("DefaultConversion") {
    it("Convert null") {
      val conversion = DefaultConversion.Instance
      conversion.convert("", classOf[Long])
      conversion.convert(null.asInstanceOf[String], classOf[Long])
      conversion.convert("abc", classOf[Long])
      conversion.convert("1", classOf[Long]).asInstanceOf[AnyRef]
      conversion.convert(1L, classOf[Long]).asInstanceOf[AnyRef]
    }
  }
}
