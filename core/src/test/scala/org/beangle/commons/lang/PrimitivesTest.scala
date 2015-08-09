/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.lang

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class PrimitivesTest extends FunSpec with Matchers {

  describe("Primitives") {
    it("default") {
      Primitives.default(classOf[Int]) should be(0)
      val d = Primitives.default(classOf[Double])
      assert(d == 0D)
      val f = Primitives.default(classOf[Float])
      assert(f == 0F)
      val i = Primitives.default(classOf[Int])
      assert(i == 0)
      val c = Primitives.default(classOf[Char])
      assert(c == '\u0000')
      val l = Primitives.default(classOf[Long])
      assert(l == 0L)
      val b = Primitives.default(classOf[Boolean])
      assert(b == false)
      val s = Primitives.default(classOf[Short])
      assert(s == 0.asInstanceOf[Short])
      val by = Primitives.default(classOf[Byte])
      assert(by == 0.asInstanceOf[Byte])
    }
    it("generate default literal"){
       assert("0F" == Primitives.defaultLiteral(classOf[Float]))
       assert("0D" == Primitives.defaultLiteral(classOf[Double]))
       assert("0" == Primitives.defaultLiteral(classOf[Int]))
       assert("0L" == Primitives.defaultLiteral(classOf[Long]))
       assert("'\u0000'" == Primitives.defaultLiteral(classOf[Char]))
       assert("false" == Primitives.defaultLiteral(classOf[Boolean]))
       assert("(short)0" == Primitives.defaultLiteral(classOf[Short]))
    }
  }
}
