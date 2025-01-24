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

import org.beangle.commons.json
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class JsonParserTest extends AnyFunSpec with Matchers {

  describe("JsonParser") {
    it("parse json ") {
      val v =
        """{"admins":["admin1","admin2"],"roles":[{"id":1,"name":"role1"},{"id":1,"name":"role2"}],
          |"hostname":"openurp.edu.cn","org":{"code":"shcm","name":"上海电影学院","id":10278},"name":"urp",
          |"id":1085601,"title":"教学系统"}""".stripMargin
      val a = Json.parseObject(v)
      assert(a.query("/org/code").contains("shcm"))
      assert(a.query("admins.[1]").contains("admin2"))
      assert(a.query("/admins/32").isEmpty)
      assert(a.query("/hostname/32").isEmpty)
      val roleNames = a.query("/roles/name")
      assert(roleNames.nonEmpty)
      assert(roleNames.get.asInstanceOf[JsonArray].contains("role2"))
      val roles = a.query("roles").get.asInstanceOf[JsonArray]
      assert(roles.query("[0].name") == "role1")
      assert(roles.query("/1/name") == "role2")
    }
    it("parse") {
      val result = Json.parseObject(
        """
                {"accountLocked":false,"details":{"isRoot":false},"authorities":[1,2],"accountExpired":false,"description":"duan","principal":"abc","credentialExpired":false,"disabled":false}
                """)
      assert(result.get("authorities").orNull.isInstanceOf[Iterable[_]])
      val a = """[{"roles":[],"scope":"Protected","name":"/config/home","title":"首页","id":94}]"""
      val data = Json.parseArray(a)
      assert(null != data)
    }
    it("isMatch") {
      val src = Json.parseObject(""" {a:1,b:{d:1}} """)
      val target = Json.parseObject(""" {a:[1,2],b:{d:[1,2]}} """)
      assert(src.isMatch(target))
    }
    it("update") {
      val data = new JsonObject()
      data.update("jobs.[1].title", "Manager")
      assert(data.query("jobs.[1].title").contains("Manager"))
    }
  }

}
