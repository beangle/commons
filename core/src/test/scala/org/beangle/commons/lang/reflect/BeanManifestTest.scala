package org.beangle.commons.lang.reflect

import java.lang.reflect.Modifier

import org.beangle.commons.lang.testbean.{ Author, Book, BookPrimitiveId, BookStore, Entity, NumIdBean }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BeanManifestTest extends FunSpec with Matchers {
  describe("BeanManifest") {
    it("find real template parameter") {
      assert(BeanManifest.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanManifest.get(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[java.lang.Object]))
      assert(BeanManifest.get(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
      assert(BeanManifest.get(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
    }

    it("ignore normal read property") {
      assert(BeanManifest.get(classOf[Book]).getGetter("empty").isDefined)
    }
    it("Can get iterface methods") {
      val method = classOf[NumIdBean[_]].getMethod("name")
      assert(Modifier.isAbstract(method.getModifiers))
      assert(BeanManifest.get(classOf[NumIdBean[_]]).getters.size == 3)

      assert(BeanManifest.get(classOf[Entity[_]]).getters.size == 3)
      assert(BeanManifest.get(classOf[Entity[_]]).getters("persisted").isTransient)
      assert(!BeanManifest.get(classOf[Entity[_]]).getters("id").isTransient)
    }

    it("Have correct virtual getter") {
      val empty = BeanManifest.get(classOf[Book]).getGetter("empty")
      assert(empty.isDefined)
      assert(empty.get.isTransient)
    }
  }
}