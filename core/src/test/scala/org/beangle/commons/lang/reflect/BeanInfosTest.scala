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

import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.reflect.TypeInfo.*
import org.beangle.commons.lang.testbean.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.lang.reflect.Modifier
import scala.collection.immutable.ArraySeq

class BeanInfosTest extends AnyFunSpec with Matchers {
  BeanInfos.of(classOf[Book])
  BeanInfos.of(classOf[BookPrimitiveId])
  BeanInfos.of(classOf[BookStore])
  BeanInfos.of(classOf[Author])
  BeanInfos.of(classOf[BigBookStore])
  BeanInfos.of(classOf[Department])
  BeanInfos.of(classOf[Menu])

  describe("BeanInfos") {
    it("find real template parameter") {
      assert(BeanInfos.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanInfos.get(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[Long]))
      assert(BeanInfos.get(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
      assert(BeanInfos.get(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
    }

    it("ignore normal read property") {
      assert(BeanInfos.get(classOf[Book]).getGetter("empty").isDefined)
    }

    it("Can get iterface methods") {
      val method = classOf[NumIdBean[_]].getMethod("name")
      assert(Modifier.isAbstract(method.getModifiers))
      assert(BeanInfos.get(classOf[NumIdBean[_]]).properties.size == 3)

      assert(BeanInfos.get(classOf[Entity[_]]).properties.size == 3)
      assert(BeanInfos.get(classOf[Entity[_]]).properties("persisted").isTransient)
      assert(BeanInfos.get(classOf[Entity[_]]).properties("id").isTransient)
    }

    it("Have correct virtual getter") {
      val empty = BeanInfos.get(classOf[Book]).properties.get("empty")
      assert(empty.isDefined)
      assert(empty.get.isTransient)
    }

    it("Have correct trait fields (template generic) type") {
      val t = BeanInfos.get(classOf[Department]).properties("parent")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[OptionType])
      assert(typeinfo.asInstanceOf[OptionType].elementType.clazz == classOf[Department])
      assert(typeinfo.asInstanceOf[OptionType].isOptional)
    }
    it("find option inner type") {
      val t = BeanInfos.get(classOf[Author]).properties("age")
      val typeinfo = t.typeinfo
      assert(typeinfo.isOptional)
      assert(typeinfo.asInstanceOf[OptionType].elementType.clazz == classOf[Int])
    }
    it("find escaped key method") {
      val t = BeanInfos.get(classOf[Author]).properties("type")
      assert(t.getter.isDefined)
    }
    it("find list author") {
      val t = BeanInfos.get(classOf[Book]).properties("authors")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[IterableType])
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Author])
    }
    it("find primitives") {
      val bm = BeanInfos.get(classOf[Book])
      val typeinfo = bm.properties("versions").typeinfo
      assert(typeinfo.isInstanceOf[IterableType])
      assert(typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Int])

      val typeinfo2 = bm.properties("versionSales").typeinfo
      assert(typeinfo2.asInstanceOf[IterableType].isMap)
      assert(typeinfo2.asInstanceOf[IterableType].args == ArraySeq(get(classOf[Int]), get(classOf[Integer])))

      val typeinfo3 = bm.properties("versionSales2").typeinfo
      assert(typeinfo3.asInstanceOf[IterableType].isMap)
      assert(typeinfo3.asInstanceOf[IterableType].args == ArraySeq(get(classOf[Int]), get(classOf[Integer])))
    }

    it("find correct get") {
      val t = BeanInfos.get(classOf[Menu])
      val getter = t.properties("id").getter
      assert(getter.isDefined)
      getter foreach { g =>
        assert(g.getName == "id")
      }
      assert(!t.properties.contains("childrenCount"))
      assert(t.properties.contains("deepSize"))
      assert(t.methods.size == 1)
    }

    it("find correct constructor info") {
      val t = BeanInfos.get(classOf[BigBookStore])
      assert(t.ctors.nonEmpty)
      val ctor = t.ctors.head
      assert(2 == ctor.parameters.size)
      assert(ctor.parameters.head.typeinfo.isInstanceOf[IterableType])
      assert(ctor.parameters.head.typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[Department])

      assert(ctor.parameters(1).typeinfo.isInstanceOf[IterableType])
      assert(ctor.parameters(1).typeinfo.asInstanceOf[IterableType].args == ArraySeq(get(classOf[String]), get(classOf[Book])))

      val p = t.properties("properties")
      assert(p.clazz == classOf[java.util.Properties])
      assert(p.typeinfo.isIterable)
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
    it("get case class method") {
      val t = BeanInfos.of(classOf[Loader])
      assert(t.properties.contains("name"))
      assert(!t.properties("name").isTransient)
    }
    it("get generic method") {
      val t = BeanInfos.of(classOf[LongFactory])
      assert(t.properties.contains("result"))
      assert(t.properties("result").getter.get.getReturnType == classOf[Long])
    }
    it("get java bean methods") {
      val t = BeanInfos.of(classOf[TestJavaBean])
      assert(!t.properties("name").isTransient)

      val t2 = BeanInfos.get(classOf[TT])
      assert(!t2.properties("name").isTransient)
    }
    it("it get primary constructor") {
      assert(BeanInfos.get(classOf[Course]).ctors.size == 2)
      assert(BeanInfos.get(classOf[Room]).ctors.size == 1)
    }
  }
}
