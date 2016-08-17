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
package org.beangle.commons.bean

import org.beangle.commons.lang.testbean.TestBean
import org.beangle.commons.lang.reflect.BeanInfos
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PropertiesTest extends FunSpec with Matchers {

  describe("Properties") {
    it("Get or Set property") {
      BeanInfos.forType(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      Properties.set(bean, "intValue", 2)
      bean.intValue should be(2)

      bean.javaMap = new java.util.HashMap[Int, String]
      Properties.copy(bean, "javaMap(1)", "2")
      bean.javaMap.get(1) should be("2")
    }

    it("get option nested value") {
      BeanInfos.forType(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      var parent = new TestBean
      parent.id = 2
      val a = Properties.get[Object](bean, "parent.id")
      assert(null == a)
      bean.parent = Some(parent)
      val b = Properties.get[Object](bean, "parent.id")
      assert(2 == b)

      Properties.set(bean, "parent.id", 4)
      val c = Properties.get[Object](bean, "parent.id")
      assert(4 == c)

      Properties.copy(bean, "parent", "")
      assert(Properties.get[Object](bean, "parent") == None)

      Properties.copy(bean, "parent", null)
      assert(Properties.get[Object](bean, "parent") == None)

      Properties.copy(bean, "parent", None)
      assert(Properties.get[Object](bean, "parent") == None)

      Properties.copy(bean, "parent", parent)
      assert(Properties.get[Object](bean, "parent") == Some(parent))

      Properties.copy(bean, "parent", Some(parent))
      assert(Properties.get[Object](bean, "parent") == Some(parent))
    }

    it("get set option[primitives]") {
      BeanInfos.forType(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      var parent = new TestBean

      Properties.set(bean, "age", 4)
      assert(bean.age == Some(4))

      Properties.set(bean, "age", null)
      assert(bean.age == None)
    }
  }
}
