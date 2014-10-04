package org.beangle.commons.web.resource.impl

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PathResolverImplTest extends FunSpec with Matchers {
  describe("PathResolverImpl") {
    it("resolve") {
      val resolver = new PathResolverImpl
      val res = resolver.resolve("/static/scripts/jquery/jquery,beangle.js")
      assert(res.size == 2)
      assert(res == List("static/scripts/jquery/jquery.js", "static/scripts/jquery/beangle.js"))
    }
  }
}