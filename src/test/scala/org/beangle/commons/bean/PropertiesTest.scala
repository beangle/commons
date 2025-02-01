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

package org.beangle.commons.bean

import org.beangle.commons.lang.reflect.BeanInfos
import org.beangle.commons.lang.testbean.{Dog, TestBean}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util

class PropertiesTest extends AnyFunSpec, Matchers {

  BeanInfos.of(classOf[TestBean])
  describe("Properties") {
    it("Get or Set property") {
      BeanInfos.get(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      Properties.set(bean, "intValue", 2)
      bean.intValue should be(2)
      bean.tags = Map("art" -> "artister", "music" -> "pop")
      bean.javaMap = new java.util.HashMap[Int, String]
      Properties.copy(bean, "javaMap(1)", "2")
      bean.javaMap.get(1) should be("2")
      Properties.get[Any](bean, "tags(music)") should equal("pop")
    }

    it("get option nested value") {
      BeanInfos.get(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      val parent = new TestBean
      parent.id = 2
      val a = Properties.get[Object](bean, "parent.id")
      assert(null == a)
      bean.parent = Some(parent)
      val b = Properties.get[Object](bean, "parent.id")
      assert(Integer.valueOf(2) == b)

      Properties.set(bean, "parent.id", 4)
      val c = Properties.get[Object](bean, "parent.id")
      assert(Integer.valueOf(4) == c)

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

      bean.javaMap = new util.HashMap[Int, String]()
      bean.javaMap.put(2, "second")
      bean.titles = Array("CTO", "CFO", "CEO")
      assert(Properties.get[Any](bean, "javaMap(2)") == "second")
      assert(Properties.get[Any](bean, "titles[2]") == "CEO")
    }

    it("get set option[primitives]") {
      BeanInfos.get(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      var parent = new TestBean

      Properties.set(bean, "age", 4)
      assert(bean.age.contains(4))

      Properties.set(bean, "age", null)
      assert(bean.age.isEmpty)
    }
    it("test scala map") {
      val p = new org.beangle.commons.collection.Properties("id" -> 1, "name" -> "mike")
      assert(Properties.get[Any](p, "id") == 1)
      assert(Properties.get[Any](p, "name") == "mike")
      Properties.set(p, "id", 2)
      assert(Properties.get[Any](p, "id") == 2)
    }
    it("test java map") {
      val p = new java.util.HashMap[Any, Any]()
      p.put("id", 1)
      p.put("name", "mike")
      assert(Properties.get[Any](p, "id") == 1)
      assert(Properties.get[Any](p, "name") == "mike")
      Properties.set(p, "id", 2)
      assert(Properties.get[Any](p, "id") == 2)
    }
    it("get collection element property name") {
      BeanInfos.get(classOf[TestBean]).properties("javaMap")
      val bean = new TestBean
      val dog1 = new Dog
      dog1.name = "dog1"
      val dog2 = new Dog
      dog2.name = "dog2"
      bean.dogs = Seq(dog1, dog2)
      assert(Properties.get[Any](bean, "dogs(name# )") == "dog1 dog2")
      assert(Properties.get[Any](bean, "dogs(name)") == "dog1,dog2")
    }
  }
}
