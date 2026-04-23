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

import java.util as ju

class JsonTest extends AnyFunSpec, Matchers {

  describe("Json") {
    it("toJson") {
      val json = JsonObject("id" -> 3L, "isManager" -> false, "salary" -> 6999,
        "age" -> Some(30), "title" -> None, "weight" -> Integer.valueOf(180), "photo" -> "this is a text photo".getBytes)
      val result = json.toJson
      assert(result.contains(""""id":"3"""))
      assert(result.contains(""""weight":180"""))
      assert(result.contains(""""isManager":false"""))
      assert(result.contains(""""title":null"""))
      assert(result.contains(""""age":3"""))
      assert(result.contains(""""photo":"dGhpcyBpcyBhIHRleHQgcGhvdG8=""""))

      val b = Json.parseObject("""{"name":"课堂测验","percent":20.3,"count":5}""")
      assert(b.toJson.contains(""""percent":20.3"""))
    }
    it("update nested creation") {
      val json = new JsonObject()
      json.update("/query/term/std/0/skills/0/name", "Play Basketball")
      json.toJson should equal("""{"query":{"term":{"std":[{"skills":[{"name":"Play Basketball"}]}]}}}""")
    }
    it("process null") {
      val obj = Json.parseObject("""{"code":"001","name":null} """)
      obj.getString("name", "33") should equal("33")
      assert(obj.toJson.contains(""""name":null"""))
    }
    it("add test") {
      val jo = new JsonObject()
      jo.add("grade", List("math" -> 90, "english" -> 80))
      jo.add("contact", Map("addr" -> "Nanjing Road 121#", "city" -> "Shanghai"))
      jo.add("card", List("king", "queue", "K"))
      jo.add("name", "jack")
      jo.add("age", 22)
      assert(jo("grade").isInstanceOf[JsonObject])
      assert(jo("contact").isInstanceOf[JsonObject])
      jo.getArray("card").size should be(3)
    }

    it("resolve path with nested brackets") {
      Json.resolvePath("a[b][c][0]") should equal(Array("a", "b", "c", "0"))
      Json.resolvePath("a.b[3].c") should equal(Array("a", "b", "3", "c"))
      Json.resolvePath("a.b.[3].c") should equal(Array("a", "b", "3", "c"))
      Json.resolvePath("/a/b/c/0") should equal(Array("a", "b", "c", "0"))
    }
    it("update with wildcard projection") {
      val data = new JsonObject()
      data.update("jobs.[1].title", "Manager")
      assert(data.query("jobs.[1].title").contains("Manager"))
      data.update("jobs[1][title]", "Manager2")
      assert(data.query("jobs[1][title]").contains("Manager2"))

      val roles = Json.parseObject("""{"roles":[{"name":"role1"},{"name":"role2"}]}""")
      roles.update("roles[*].name", "guest")
      assert(roles.query("roles[0].name").contains("guest"))
      assert(roles.query("roles[1].name").contains("guest"))

      val matrix = Json.parseObject("""{"matrix":[[1,2],[3,4]]}""")
      matrix.update("matrix[*][*]", 0)
      assert(matrix.query("matrix[0][0]").contains(0))
      assert(matrix.query("matrix[0][1]").contains(0))
      assert(matrix.query("matrix[1][0]").contains(0))
      assert(matrix.query("matrix[1][1]").contains(0))
    }

    it("update supports valid negative index") {
      val roles = Json.parseObject("""{"roles":[{"name":"role1"},{"name":"role2"}]}""")
      roles.update("roles[-1].name", "guest")
      assert(roles.query("roles[0].name").contains("role1"))
      assert(roles.query("roles[1].name").contains("guest"))
    }

    it("update should throw exception on out-of-range negative index") {
      val roles = Json.parseObject("""{"roles":[{"name":"role1"},{"name":"role2"}]}""")
      assertThrows[IllegalArgumentException] {
        roles.update("roles[-3].name", "guest")
      }
    }

    it("update should throw exception on illegal index token") {
      val roles = Json.parseObject("""{"roles":[{"name":"role1"},{"name":"role2"}]}""")
      assertThrows[IllegalArgumentException] {
        roles.update("roles[abc].name", "guest")
      }
    }

    it("update should throw exception on object wildcard projection") {
      val profile = Json.parseObject("""{"profile":{"name":"jack"}}""")
      assertThrows[IllegalArgumentException] {
        profile.update("profile[*].name", "guest")
      }
    }

    it("update should throw exception on invalid path") {
      val data = Json.parseObject("""{"roles":[{"name":"role1"}]}""")
      assertThrows[IllegalArgumentException] {
        data.update("roles.name", "guest")
      }
    }

    it("of should support java map and collection") {
      val author1 = new ju.HashMap[String, Any]()
      author1.put("name", "Jack")
      val author2 = new ju.HashMap[String, Any]()
      author2.put("name", "Mike")
      val authors = new ju.ArrayList[Any]()
      authors.add(author1)
      authors.add(author2)
      val book = new ju.HashMap[String, Any]()
      book.put("authors", authors)

      val json = Json.of(book)
      json.isInstanceOf[JsonObject] should be(true)
      val jo = json.asInstanceOf[JsonObject]
      jo.query("authors[*].name").get.asInstanceOf[JsonArray] should equal(JsonArray("Jack", "Mike"))
    }

    it("update should handle null value") {
      val data = Json.parseObject("""{"roles":[{"name":"role1"}]}""")
      data.update("roles[0].name", null)
      data.query("roles[0].name").isEmpty should be(true)
    }
  }
}
