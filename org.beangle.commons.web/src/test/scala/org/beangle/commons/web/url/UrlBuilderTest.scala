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

import org.testng.Assert.assertEquals
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

/**
 * @author chaostone
 * @version $Id: UrlBuilderTest.java Nov 13, 2010 9:39:00 AM chaostone $
 */
@Test
class UrlBuilderTest {

  def buildFullUrl() {
    val builder = new UrlBuilder("/")
    builder.scheme("http").serverName("localhost").port(80)
    builder.requestURI("/demo/security/user")
    builder.queryString("name=1&fullname=join")
    assertEquals(builder.buildUrl(), "http://localhost/demo/security/user?name=1&fullname=join")
    builder.requestURI(null).port(8080).servletPath("/security")
    assertEquals(builder.buildUrl(), "http://localhost:8080/security?name=1&fullname=join")
  }

  def build() {
    val builder = new UrlBuilder("/")
    builder.servletPath("/security/user")
    builder.queryString("name=1&fullname=join")
    assertEquals(builder.buildRequestUrl(), "/security/user?name=1&fullname=join")
    builder.requestURI("/demo/security/user")
    assertEquals(builder.buildRequestUrl(), "/security/user?name=1&fullname=join")
  }
}
