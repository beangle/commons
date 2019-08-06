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
package org.beangle.commons.web.util

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CookieGeneratorTest extends AnyFunSpec with Matchers {

  describe("Cookieg") {
    it("set base with all part") {
      val g = new CookieGenerator("sid")
      g.base = "localhost:9080/myApp"
      g.domain should be("localhost")
      g.path should be("/myApp")
      g.port should be(9080)
      g.base should be("http://localhost:9080/myApp")
    }

    it("set simple base") {
      val g = new CookieGenerator("sid")
      g.base = "jwxt.openurp.edu.cn"
      g.domain should be("jwxt.openurp.edu.cn")
      g.path should be("/")
      g.port should be(80)
      g.base should be("http://jwxt.openurp.edu.cn/")
    }

    it("set simple https base with path") {
      val g = new CookieGenerator("sid")
      g.base = "https://localhost/a"
      g.domain should be("localhost")
      g.path should be("/a")
      g.port should be(443)
      g.base should be("https://localhost/a")
    }

    it("set simple https base with path and port") {
      val g = new CookieGenerator("sid")
      g.base = "https://localhost:9443/"
      g.domain should be("localhost")
      g.path should be("/")
      g.port should be(9443)
      g.base should be("https://localhost:9443/")
    }
  }

}
