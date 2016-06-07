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
package org.beangle.commons.lang.reflect

import java.lang.reflect.Modifier
import org.beangle.commons.lang.testbean.{ Author, Book, BookPrimitiveId, BookStore, Entity, NumIdBean }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.testbean.Department
import org.beangle.commons.lang.testbean.BigBookStore
import org.beangle.commons.cache.concurrent.ConcurrentMapCacheManager

@RunWith(classOf[JUnitRunner])
class BeanManifestTest extends FunSpec with Matchers {
  describe("BeanManifest") {
    it("find real template parameter") {
      assert(BeanManifest.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanManifest.forType(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[Long]))
      assert(BeanManifest.get(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
      assert(BeanManifest.get(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
    }

    it("ignore normal read property") {
      assert(BeanManifest.get(classOf[Book]).getGetter("empty").isDefined)
    }
    it("Can get iterface methods") {
      val method = classOf[NumIdBean[_]].getMethod("name")
      assert(Modifier.isAbstract(method.getModifiers))
      assert(BeanManifest.get(classOf[NumIdBean[_]]).properties.size == 3)

      assert(BeanManifest.get(classOf[Entity[_]]).properties.size == 3)
      assert(BeanManifest.get(classOf[Entity[_]]).properties("persisted").isTransient)
      assert(!BeanManifest.get(classOf[Entity[_]]).properties("id").isTransient)
    }

    it("Have correct virtual getter") {
      val empty = BeanManifest.get(classOf[Book]).properties.get("empty")
      assert(empty.isDefined)
      assert(empty.get.isTransient)
    }

    it("Have correct trait fields") {
      val empty = BeanManifest.forType(classOf[Department]).properties.get("parent")
      assert(empty.isDefined)
      assert(empty.get.clazz == classOf[Department])
    }

    it("not null implicit value") {
      val clazz = classOf[Department]
      fun(clazz)
    }
    it("find list author") {
      val t = BeanManifest.get(classOf[Book]).properties("authors")
      val typeinfo = t.typeinfo
      assert(typeinfo.isInstanceOf[CollectionType])
      assert(typeinfo.asInstanceOf[CollectionType].componentType == classOf[Author])
    }
    it("find corrent constructor info") {
      val t = BeanManifest.get(classOf[BigBookStore])
      assert(!t.constructors.isEmpty)
      val ctor = t.constructors.head
      assert(2 == ctor.args.size)
      assert(ctor.args.head.isInstanceOf[CollectionType])
      assert(ctor.args.head.asInstanceOf[CollectionType].componentType == classOf[Department])

      assert(ctor.args(1).isInstanceOf[MapType])
      assert(ctor.args(1).asInstanceOf[MapType].keyType == classOf[String])
      assert(ctor.args(1).asInstanceOf[MapType].valueType == classOf[Book])

      val p = t.properties("properties")
      assert(p.clazz == classOf[java.util.Properties])
      assert(p.typeinfo.isMapType)
      assert(p.typeinfo.asInstanceOf[MapType].keyType == classOf[Object])
      assert(p.typeinfo.asInstanceOf[MapType].valueType == classOf[Object])

      val prices = t.properties("prices")
      assert(prices.clazz == classOf[Range])
      assert(prices.typeinfo.isCollectionType)
      assert(prices.typeinfo.asInstanceOf[CollectionType].componentType == classOf[Object])

      val p2 = t.properties("properties2")
      assert(p2.clazz == classOf[org.beangle.commons.collection.Properties])
      assert(p2.typeinfo.isMapType)
      assert(p2.typeinfo.asInstanceOf[MapType].keyType == classOf[String])
      assert(p2.typeinfo.asInstanceOf[MapType].valueType == classOf[Object])
    }
    it("find corrent default constructor parameters") {
      val params = BeanManifest.get(classOf[ConcurrentMapCacheManager]).defaultConstructorParams
      assert(params.size == 1)
      assert(params(1) == "concurrent")
    }
  }

  def fun(c: Class[_]): BeanManifest = {
    BeanManifest.get(c)
  }
}