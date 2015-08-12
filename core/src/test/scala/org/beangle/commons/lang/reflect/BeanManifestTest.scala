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
package org.beangle.commons.lang.reflect

import java.lang.reflect.Modifier
import org.beangle.commons.lang.testbean.{ Author, Book, BookPrimitiveId, BookStore, Entity, NumIdBean }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.testbean.Department

@RunWith(classOf[JUnitRunner])
class BeanManifestTest extends FunSpec with Matchers {
  describe("BeanManifest") {
    it("find real template parameter") {
      assert(BeanManifest.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanManifest.get(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[Long]))
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
      val empty = BeanManifest.get(classOf[Department]).properties.get("parent")
      assert(empty.isDefined)
      assert(empty.get.clazz == classOf[Department])
    }

    it("not null implicit value") {
      val clazz = classOf[Department]
      fun(clazz)
    }
  }

  def fun(c: Class[_]): BeanManifest = {
    BeanManifest.get(c)
  }
}