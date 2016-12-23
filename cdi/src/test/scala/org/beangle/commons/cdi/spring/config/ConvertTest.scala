/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cdi.spring.config

import java.math.BigDecimal
import org.scalatest.{FunSpec, Matchers}
import org.springframework.core.convert.support.DefaultConversionService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConvertTest extends FunSpec with Matchers {

  describe("Spring") {
    it("Convert number and boolean") {
      val conversion = new DefaultConversionService();
      conversion.convert("4.5", classOf[Number]) should equal(new BigDecimal("4.5"))
      conversion.convert("true", classOf[Boolean]) should be(true)
    }
  }
}
