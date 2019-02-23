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
package org.beangle.commons.lang.reflect

import java.lang.reflect.Modifier
import org.beangle.commons.lang.testbean.{ Author, Book, BookPrimitiveId, BookStore, Entity, NumIdBean }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.testbean.Department
import org.beangle.commons.lang.testbean.BigBookStore
import org.beangle.commons.collection.Properties

@RunWith(classOf[JUnitRunner])
class BeanInfosTest extends FunSpec with Matchers {
  describe("BeanInfos") {
    it("find real template parameter") {
      assert(BeanInfos.forType(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanInfos.forType(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[Long]))
      assert(BeanInfos.forType(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
      assert(BeanInfos.forType(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
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
      assert(!BeanInfos.get(classOf[Entity[_]]).properties("id").isTransient)
    }

    it("Have correct virtual getter") {
      val empty = BeanInfos.get(classOf[Book]).properties.get("empty")
      assert(empty.isDefined)
      assert(empty.get.isTransient)
    }

    it("Have correct trait fields (template generic) type") {
      val t = BeanInfos.forType(classOf[Department]).properties("parent")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[ElementType])
      assert(typeinfo.asInstanceOf[ElementType].clazz == classOf[Department])
      assert(typeinfo.asInstanceOf[ElementType].optional)
    }
    it("find option inner type") {
      val t = BeanInfos.get(classOf[Author]).properties("age")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[ElementType])
      assert(typeinfo.asInstanceOf[ElementType].clazz == classOf[Int])
    }
    it("find escaped key method") {
      val t = BeanInfos.get(classOf[Author]).properties("type")
      assert(t.getter.isDefined)
    }
    it("find list author") {
      val t = BeanInfos.get(classOf[Book]).properties("authors")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[CollectionType])
      assert(typeinfo.asInstanceOf[CollectionType].elementType == classOf[Author])
    }
    it("find primitives") {
      val bm = BeanInfos.get(classOf[Book])
      val typeinfo = bm.properties("versions").typeinfo
      assert(typeinfo.isInstanceOf[CollectionType])
      assert(typeinfo.asInstanceOf[CollectionType].elementType == classOf[Int])

      val typeinfo2 = bm.properties("versionSales").typeinfo
      assert(typeinfo2.isInstanceOf[MapType])
      assert(typeinfo2.asInstanceOf[MapType].keyType == classOf[Int])
      assert(typeinfo2.asInstanceOf[MapType].valueType == classOf[Integer])

      val typeinfo3 = bm.properties("versionSales2").typeinfo
      assert(typeinfo3.isInstanceOf[MapType])
      assert(typeinfo3.asInstanceOf[MapType].keyType == classOf[Int])
      assert(typeinfo3.asInstanceOf[MapType].valueType == classOf[Integer])
    }
    it("find corrent constructor info") {
      val t = BeanInfos.get(classOf[BigBookStore])
      assert(!t.constructors.isEmpty)
      val ctor = t.constructors.head
      assert(2 == ctor.args.size)
      assert(ctor.args.head.isInstanceOf[CollectionType])
      assert(ctor.args.head.asInstanceOf[CollectionType].elementType == classOf[Department])

      assert(ctor.args(1).isInstanceOf[MapType])
      assert(ctor.args(1).asInstanceOf[MapType].keyType == classOf[String])
      assert(ctor.args(1).asInstanceOf[MapType].valueType == classOf[Book])

      val p = t.properties("properties")
      assert(p.clazz == classOf[java.util.Properties])
      assert(p.typeinfo.isMap)
      assert(p.typeinfo.asInstanceOf[MapType].keyType == classOf[Object])
      assert(p.typeinfo.asInstanceOf[MapType].valueType == classOf[Object])

      val prices = t.properties("prices")
      assert(prices.clazz == classOf[Range])
      assert(prices.typeinfo.isCollection)
      assert(prices.typeinfo.asInstanceOf[CollectionType].elementType == classOf[Object])

      val p2 = t.properties("properties2")
      assert(p2.clazz == classOf[org.beangle.commons.collection.Properties])
      assert(p2.typeinfo.isMap)
      assert(p2.typeinfo.asInstanceOf[MapType].keyType == classOf[String])
      assert(p2.typeinfo.asInstanceOf[MapType].valueType == classOf[Object])

      val p3 = t.properties("tempName")
      assert(p3.isTransient)
    }
    //    it("find corrent default constructor parameters") {
    //      val params = BeanInfos.get(classOf[ConcurrentMapCacheManager]).defaultConstructorParams
    //      assert(params.size == 1)
    //      assert(params(1) == "concurrent")
    //    }
    it("find scala native type beaninfos") {
      val p = new Properties("id" -> 1, "name" -> "john")
      val pBeanInfo = BeanInfos.get(p)

    }
  }

}
