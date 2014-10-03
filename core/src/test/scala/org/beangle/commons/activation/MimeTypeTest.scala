package org.beangle.commons.activation

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.ClassLoaders.{ getResource, getResources }

@RunWith(classOf[JUnitRunner])
class MimeTypeTest extends FunSpec with Matchers {
  describe("MimeType") {
    it("load resource") {
      val resources = new Resources(getResource("org/beangle/commons/activation/mime_test.types"),
        getResources("META-INF/mime_test.types"), getResource("mime_test.types"))
      val map = MimeTypes.buildMimeTypes(resources)
      assert(map.size == 5)
      assert(None != map.get("xxx"))
    }
    it("parse") {
      val mimeTypes = MimeTypes.parse("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      assert(4 == mimeTypes.size)
    }
  }

  describe("MimeTypeProvider") {
    it("load resource") {
      val xlsx = MimeTypeProvider.getMimeType("xlsx")
      assert(None != xlsx)
      assert(xlsx.get.getSubType == "vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }
  }
}