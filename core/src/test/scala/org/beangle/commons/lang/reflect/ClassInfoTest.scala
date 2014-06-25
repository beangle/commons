package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.testbean.{ Book, BookPrimitiveId, BookStore }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ClassInfoTest extends FunSpec with Matchers {
  describe("ClassInfo") {
    it("find real template parameter") {
      assert(ClassInfo.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(ClassInfo.get(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[java.lang.Object]))
      assert(ClassInfo.get(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))
    }
  }
}