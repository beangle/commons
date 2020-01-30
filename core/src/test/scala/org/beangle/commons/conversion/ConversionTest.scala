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
package org.beangle.commons.conversion
import org.beangle.commons.conversion.impl.DefaultConversion
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConversionTest extends AnyFunSpec with Matchers {

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
