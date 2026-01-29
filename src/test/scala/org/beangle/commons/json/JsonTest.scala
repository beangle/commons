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

package org.beangle.commons.json

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class JsonTest extends AnyFunSpec, Matchers {

  describe("Json") {
    it("toJson") {
      val json = JsonObject("id" -> 3L, "isManager" -> false, "salary" -> 6999,
        "age" -> Some(30), "title" -> None, "weight" -> Integer.valueOf(180))
      val result = json.toJson
      assert(result.contains(""""id":"3"""))
      assert(result.contains(""""weight":180"""))
      assert(result.contains(""""isManager":false"""))
      assert(result.contains(""""title":null"""))
      assert(result.contains(""""age":3"""))

      val b = Json.parseObject("""{"name":"课堂测验","percent":20.3,"count":5}""")
      assert(b.toJson.contains(""""percent":20.3"""))
    }
    it("update") {
      val json = new JsonObject()
      json.update("/query/term/std/0/skills/0/name", "Play Basketball")
      json.toJson should equal("""{"query":{"term":{"std":[{"skills":[{"name":"Play Basketball"}]}]}}}""")
    }
  }
}
