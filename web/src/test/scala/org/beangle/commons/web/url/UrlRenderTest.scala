/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
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
      val render = new UrlRender(".html")
      val uri = "/demo/security/user!list.html"
      render.render(uri, "user") should be equals ("/demo/security/user.html")
      render.render(uri, "user!search") should be equals ("/demo/security/user!search.html")
      render.render(uri, "!save") should be equals ("/demo/security/user!save.html")
      render.render(uri, "user!search?id=1") should be equals ("/demo/security/user!search.html?id=1")
      render.render(uri, "/database/query!history?id=1") should be equals ("/demo/database/query!history.html?id=1")
    }

    it("testRenderEmptyContext") {
      val render = new UrlRender()
      val uri = "/user!list"
      render.render(uri, "user") should be equals ("/user")
      render.render(uri, "user!search") should be equals ("/user!search")
      render.render(uri, "!save") should be equals ("/user!save")
      render.render(uri, "user!search?id=1") should be equals ("/user!search?id=1")
      render.render(uri, "/database/query!history?id=1") should be equals ("/database/query!history?id=1")
    }
  }
}
