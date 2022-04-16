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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ClassLoadersTest  extends AnyFunSpec with Matchers {

  describe("ClassLoaders") {
    it("load primatives") {
      classOf[Int] should be (ClassLoaders.load("Int"))
      classOf[Int] should be (ClassLoaders.load("int"))
      classOf[Unit] should be (ClassLoaders.load("void"))
      classOf[Unit] should be (ClassLoaders.load("Unit"))
      classOf[Integer] should be (ClassLoaders.load("Integer"))
    }
  }

}
