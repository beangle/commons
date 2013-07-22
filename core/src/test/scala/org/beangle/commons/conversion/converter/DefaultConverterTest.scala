/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.beangle.commons.conversion.Conversion
import org.beangle.commons.conversion.impl.DefaultConversion

class DefaultConverterTest extends FunSpec with ShouldMatchers {

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
