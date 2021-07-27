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

package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.testbean.NumIdBean
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.beangle.commons.lang.testbean.*
import org.beangle.commons.collection.Properties
import java.lang.reflect.Modifier
import org.beangle.commons.lang.reflect.TypeInfo.*

class BeanInfosTest extends AnyFunSpec with Matchers {
  BeanInfos.of(classOf[Book])
  BeanInfos.of(classOf[BookPrimitiveId])
  BeanInfos.of(classOf[BookStore])
  BeanInfos.of(classOf[Author])
  BeanInfos.of(classOf[BigBookStore])
  BeanInfos.of(classOf[Department])

  describe("BeanInfos") {
    it("transient persisted property") {
      val mis = BeanInfos.load(classOf[NumIdBean[_]]).getMethods("persisted")
      assert(mis.size == 1)
      val mi = mis.head
      val anns = mi.method.getAnnotations
      assert(null != anns && anns.length == 1)
    }

    it("find real template parameter") {
      assert(BeanInfos.load(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanInfos.load(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[Long]))
      assert(BeanInfos.load(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
      assert(BeanInfos.load(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
    }

    it("ignore normal read property") {
      assert(BeanInfos.load(classOf[Book]).getGetter("empty").isDefined)
    }

    it("Can get iterface methods") {
      val method = classOf[NumIdBean[_]].getMethod("name")
      assert(Modifier.isAbstract(method.getModifiers))
      assert(BeanInfos.load(classOf[NumIdBean[_]]).properties.size == 3)

      assert(BeanInfos.load(classOf[Entity[_]]).properties.size == 3)
      assert(BeanInfos.load(classOf[Entity[_]]).properties("persisted").isTransient)
      assert(BeanInfos.load(classOf[Entity[_]]).properties("id").isTransient)
    }

    it("Have correct virtual getter") {
      val empty = BeanInfos.load(classOf[Book]).properties.get("empty")
      assert(empty.isDefined)
      assert(empty.get.isTransient)
    }

    it("Have correct trait fields (template generic) type") {
      val t = BeanInfos.load(classOf[Department]).properties("parent")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[IterableType])
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Department])
      assert(typeinfo.asInstanceOf[IterableType].isOptional)
    }
    it("find option inner type") {
      val t = BeanInfos.load(classOf[Author]).properties("age")
      val typeinfo = t.typeinfo
      assert(typeinfo.isOptional)
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Int])
    }
    it("find escaped key method") {
      val t = BeanInfos.load(classOf[Author]).properties("type")
      assert(t.getter.isDefined)
    }
    it("find list author") {
      val t = BeanInfos.load(classOf[Book]).properties("authors")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[IterableType])
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Author])
    }
    it("find primitives") {
      val bm = BeanInfos.load(classOf[Book])
      val typeinfo = bm.properties("versions").typeinfo
      assert(typeinfo.isInstanceOf[IterableType])
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Int])

      val typeinfo2 = bm.properties("versionSales").typeinfo
      assert(typeinfo2.asInstanceOf[IterableType].isMap)
      assert(typeinfo2.asInstanceOf[IterableType].args == List(get(classOf[Int]), get(classOf[Integer])))

      val typeinfo3 = bm.properties("versionSales2").typeinfo
      assert(typeinfo3.asInstanceOf[IterableType].isMap)
      assert(typeinfo3.asInstanceOf[IterableType].args == List(get(classOf[Int]), get(classOf[Integer])))
    }

    it("find correct get") {
      val t = BeanInfos.load(classOf[Menu])
      val getter = t.properties("id").getter
      assert(getter.isDefined)
      getter foreach { g =>
        assert(g.getName == "id")
      }
    }

    it("find correct constructor info") {
      val t = BeanInfos.load(classOf[BigBookStore])
      assert(t.ctors.nonEmpty)
      val ctor = t.ctors.head
      assert(2 == ctor.parameters.size)
      assert(ctor.parameters.head.typeinfo.isInstanceOf[IterableType])
      assert(ctor.parameters.head.typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Department])

      assert(ctor.parameters(1).typeinfo.isInstanceOf[IterableType])
      assert(ctor.parameters(1).typeinfo.asInstanceOf[IterableType].args == List(get(classOf[String]), get(classOf[Book])))

      val p = t.properties("properties")
      assert(p.clazz == classOf[java.util.Properties])
      assert(p.typeinfo.asInstanceOf[IterableType].isMap)
      assert(p.typeinfo.asInstanceOf[IterableType].args == List(get(classOf[Object]), get(classOf[Object])))

      val prices = t.properties("prices")
      assert(prices.clazz == classOf[Range])
      assert(prices.typeinfo.isInstanceOf[IterableType])
      assert(prices.typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Object])

      val p2 = t.properties("properties2")
      assert(p2.clazz == classOf[org.beangle.commons.collection.Properties])
      assert(p2.typeinfo.asInstanceOf[IterableType].isMap)
      assert(p2.typeinfo.asInstanceOf[IterableType].args == List(get(classOf[String]), get(classOf[Object])))

      val p3 = t.properties("tempName")
      assert(p3.isTransient)
    }
  }
}
