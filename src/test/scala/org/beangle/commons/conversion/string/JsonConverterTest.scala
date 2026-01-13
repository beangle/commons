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

package org.beangle.commons.conversion.string

import org.beangle.commons.json.{JsonArray, JsonObject}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class JsonConverterTest extends AnyFunSpec, Matchers {
  describe("JsonConverter") {
    it("Convert String to ju.date") {
      val converter = JsonConverter.getConverter(classOf[JsonObject]).orNull
      val json = converter.apply("""{"id":1,"name":"firefox"}""")
      assert(json.contains("name"))

      val aConverter = JsonConverter.getConverter(classOf[JsonArray]).orNull
      val array = aConverter.apply("""[{"id":1,"name":"firefox"}]""")
      assert(array.size == 1)

    }
  }
}
