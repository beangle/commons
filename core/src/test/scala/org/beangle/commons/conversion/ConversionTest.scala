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
package org.beangle.commons.conversion
import org.scalatest.FunSpec
import org.scalatest.Matchers

import org.beangle.commons.conversion.impl.DefaultConversion

class ConversionTest extends FunSpec with Matchers {

  val con = new DefaultConversion();

  describe("DefaultConversion") {
    it("Convert Integer") {
      con.convert(2.5f, classOf[Integer])
    }

    it("Convert Array") {
      con.convert(Array("2", "3"), classOf[Array[Integer]])
    }

    it("Convert Primitive") {
      con.convert("2", classOf[Int]).toInt
      con.convert(3, classOf[Integer])
    }

    it("Convert Primitive Array") {
      val con = new DefaultConversion()
      con.convert(Array("2", "3.4"), classOf[Array[Float]]).asInstanceOf[Array[Float]]
    }
  }
}
