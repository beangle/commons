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
package org.beangle.commons.web.url

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UrlRenderTest extends FunSpec with Matchers {

  describe("UrlRender") {
    it("testRender") {
      val render = new UrlRender
      val context = "/demo"
      val uri = "/security/"
      assert(render.render(context, uri, "user.html") == "/demo/security/user.html")
      assert(render.render(context, uri, "user!search.html") == "/demo/security/user!search.html")
      assert(render.render(context, uri, "!save.html") == "/demo/security/!save.html")
      assert(render.render(context, uri, "user!search.html?id=1") == "/demo/security/user!search.html?id=1")
      assert(render.render(context, uri, "/database/query!history.html?id=1") == "/demo/database/query!history.html?id=1")
    }

    it("testRenderEmptyContext") {
      val render = new UrlRender()
      val uri = "/user!list"
      assert(render.render("", uri, "user") == "/user")
      assert(render.render("", uri, "user!search") == "/user!search")
      assert(render.render("", uri, "!save") == "/user!save")
      assert(render.render("", uri, "user!search?id=1") == "/user!search?id=1")
      assert(render.render("", "/user/info?id=1", "/database/query!history?id=1") == "/database/query!history?id=1")
      assert(render.render("", uri, "/database/query!history?id=1") == "/database/query!history?id=1")
    }
  }
}
