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

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.config.Resources
import org.beangle.commons.lang.ClassLoaders.{ getResource, getResources }

@RunWith(classOf[JUnitRunner])
class MimeTypeTest extends FunSpec with Matchers {
  describe("MimeType") {
    it("load resource") {
      val resources = new Resources(getResource("org/beangle/commons/activation/mime_test.types"),
        getResources("META-INF/mime_test.types"), getResource("mime_test.types"))
      val map = MimeTypes.buildMimeTypes(resources)
      assert(map.size == 10)
      assert(None != map.get("xxx"))
    }
    it("parse") {
      val mimeTypes = MimeTypes.parse("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      assert(4 == mimeTypes.size)
    }
  }

  describe("MimeTypeProvider") {
    it("load resource") {
      val xlsx = MimeTypes.getMimeType("xlsx")
      assert(None != xlsx)
      assert(xlsx.get.getSubType == "vnd.openxmlformats-officedocument.spreadsheetml.sheet")

      val all = MimeTypes.getMimeType("*/*")
      assert(Some(MimeTypes.All) == all)

      val csv = MimeTypes.parse("text/csv").head
      assert(MimeTypes.TextCsv == csv)

    }
  }
}
