package org.beangle.commons.io

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceResolverTest extends FunSpec with Matchers {
  describe("ResourceResolver") {
    it("getResources") {
      val resolver = new ResourcePatternResolver
      val rs = resolver.getResources("classpath*:META-INF/**/pom.properties")
      assert(!rs.isEmpty)
      
      val rs1 = resolver.getResources("classpath:META-INF/maven/org.slf4j/slf4j-api/pom.properties")
      assert(!rs1.isEmpty)
    }
  }
}