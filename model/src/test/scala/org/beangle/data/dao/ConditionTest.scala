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
package org.beangle.data.dao

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.reflect.ClassInfo

@RunWith(classOf[JUnitRunner])
class ConditionTest extends FunSpec with Matchers {

  describe("Condition") {
    it("paramNames should given list string") {
      val con = new Condition("a.id=:id and b.name=:name")
      val paramNames = con.paramNames
      assert(null != paramNames)
      assert(paramNames.size == 2)
      assert(paramNames.contains("id"))
      assert(paramNames.contains("name"))

      val con2 = new Condition(":beginOn < a.beginOn and b.name=:name")
      val paramNames2 = con2.paramNames
      assert(null != paramNames)
      assert(paramNames2.size == 2)
      assert(paramNames2.contains("beginOn"))
      assert(paramNames2.contains("name"))
    }
  }
}
