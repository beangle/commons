package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.testbean.{ Book, BookPrimitiveId, BookStore }
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.testbean.Author

@RunWith(classOf[JUnitRunner])
class BeanManifestTest extends FunSpec with Matchers {
  describe("BeanManifest") {
    it("find real template parameter") {
      assert(BeanManifest.get(classOf[Book]).getPropertyType("id") == Some(classOf[java.lang.Long]))
      assert(BeanManifest.get(classOf[BookPrimitiveId]).getPropertyType("id") == Some(classOf[java.lang.Object]))
      assert(BeanManifest.get(classOf[BookStore]).getPropertyType("id") == Some(classOf[String]))     
      assert(BeanManifest.get(classOf[Author]).getPropertyType("id") == Some(classOf[Integer]))
    }
  }
}