/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.web.url

import org.testng.Assert
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class UrlRenderTest {

  def testRender() {
    val render = new UrlRender(".html")
    val uri = "/demo/security/user!list.html"
    var result = render.render(uri, "user")
    result
    result = render.render(uri, "user!search")
    result
    result = render.render(uri, "!save")
    result
    result = render.render(uri, "user!search?id=1")
    result
    result = render.render(uri, "/database/query!history?id=1")
    result
  }

  def testRenderEmptyContext() {
    val render = new UrlRender()
    val uri = "/user!list"
    var result = render.render(uri, "user")
    result
    result = render.render(uri, "user!search")
    result
    result = render.render(uri, "!save")
    result
    result = render.render(uri, "user!search?id=1")
    result
    result = render.render(uri, "/database/query!history?id=1")
    result
  }
}
