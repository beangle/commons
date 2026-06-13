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

package org.beangle.commons.activation

import org.beangle.commons.io.Resources
import org.beangle.commons.lang.ClassLoaders.{getResource, getResources}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MediaTypeTest extends AnyFunSpec, Matchers {
  val resources = Resources.load("org/beangle/commons/activation/mime_test.types,META-INF/mime_test.types,mime_test.types")
  val types = MediaTypes.build(resources)
  val mimetypes= new MediaTypes(types)

  describe("MediaType") {
    it("load resource") {
      assert(types.size == 10)
      assert(types.get("xxx").isDefined)
    }
    it("parse") {
      val mimeTypes = MediaTypes.parse("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      assert(4 == mimeTypes.size)
    }
  }

  describe("MediaTypeProvider") {
    it("load mime-default.types") {
      val defaults = new MediaTypes(MediaTypes.Defaults)

      val xlsx = defaults.get("xlsx")
      assert(xlsx.isDefined)
      assert(xlsx.get.subType == "vnd.openxmlformats-officedocument.spreadsheetml.sheet")

      val all = defaults.get("*/*")
      assert(all.contains(MediaTypes.All))

      val csv = MediaTypes.parse("text/csv").head
      assert(defaults.get("text/csv").contains(csv))
      assert(MediaTypes.csv == csv)

      val ofd = MediaTypes.parse("application/ofd").head
      assert(defaults.get("application/ofd").contains(ofd))
      assert(MediaTypes.ofd == ofd)
    }

    it("build adds wildcard") {
      assert(mimetypes.get("*/*").contains(MediaTypes.All))
    }
  }
}
