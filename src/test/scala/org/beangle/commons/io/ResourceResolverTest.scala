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

package org.beangle.commons.io

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ResourceResolverTest extends AnyFunSpec, Matchers {
  describe("ResourceResolver") {
    it("getResources") {
      val resolver = new ResourcePatternResolver
      val rs = resolver.getResources("META-INF/**/pom.properties")
      assert(rs.nonEmpty)

      val rs1 = resolver.getResources("META-INF/maven/org.slf4j/slf4j-api/pom.properties")
      assert(rs1.nonEmpty)
    }
    it("version loader") {
      val loader = new ResourceVersionLoader()
      val resourceName = "org/beangle/commons/io/template.txt"
      val version2 = loader.getResource(resourceName, "2")
      version2.nonEmpty shouldBe (true)
      val version3 = loader.getResource(resourceName, "3")
      version3.nonEmpty shouldBe (true)
      val version4 = loader.getResource(resourceName, "4")
      assert(version3 == version4)

      val version6 = loader.getResource(resourceName, "6")
      assert(version6.nonEmpty)
      assert(version6.get.getFile.contains("template.txt"))
    }
  }
}
