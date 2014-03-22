/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.lang.reflect

import java.lang.reflect.Method
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BridgeMethodTest extends FunSpec with Matchers {

  describe("Reflection class bridge method") {
    it("Find bridge methods") {
      for (m <- classOf[Dog].getMethods if m.getName == "getAge" && null == m.getReturnType) {
        if (m.getReturnType == classOf[Integer]) m.isBridge else if (m.getReturnType == classOf[Number]) {
          m.isBridge
          m.getDeclaringClass
        }
      }
    }
  }
}

trait Animal {

  def getAge(): Number
}

class Dog extends Animal {

  def getAge(): java.lang.Integer = 0
}
