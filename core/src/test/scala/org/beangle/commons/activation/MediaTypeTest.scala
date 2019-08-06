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
package org.beangle.commons.activation

import org.beangle.commons.config.Resources
import org.beangle.commons.lang.ClassLoaders.{getResource, getResources}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MediaTypeTest extends AnyFunSpec with Matchers {
  describe("MediaType") {
    it("load resource") {
      val resources = new Resources(getResource("org/beangle/commons/activation/mime_test.types"),
        getResources("META-INF/mime_test.types"), getResource("mime_test.types"))
      val map = MediaTypes.buildTypes(resources)
      assert(map.size == 10)
      assert(None != map.get("xxx"))
    }
    it("parse") {
      val mimeTypes = MediaTypes.parse("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      assert(4 == mimeTypes.size)
    }
  }

  describe("MediaTypeProvider") {
    it("load resource") {
      val xlsx = MediaTypes.get("xlsx")
      assert(None != xlsx)
      assert(xlsx.get.subType == "vnd.openxmlformats-officedocument.spreadsheetml.sheet")

      val all = MediaTypes.get("*/*")
      assert(Some(MediaTypes.All) == all)

      val csv = MediaTypes.parse("text/csv").head
      assert(MediaTypes.TextCsv == csv)

    }
  }
}
