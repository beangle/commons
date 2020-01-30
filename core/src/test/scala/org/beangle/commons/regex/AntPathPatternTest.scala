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
package org.beangle.commons.regex

import org.beangle.commons.regex.AntPathPattern._
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AntPathPatternTest extends AnyFunSpec with Matchers {

  describe("AntPathPattern") {
    it("Match ant pattern expression") {
      matches("com/t?st.jsp", "com/test.jsp") should be(true)
      matches("com/*.jsp", "com/test.jsp")
      matches("com/*.jsp", "com/dir/test.jsp")
      matches("com/**/test.jsp", "com/dir1/dir2/test.jsp")
      matches("com/beangle/**/*.jsp", "com/beangle/dir1/dir2/test3.jsp")
      matches("org/**/servlet/bla.jsp", "org/beangle/servlet/bla.jsp")
      matches("org/**/servlet/bla.jsp", "org/beangle/testing/servlet/bla.jsp")
      matches("org/**/servlet/bla.jsp", "org/servlet/bla.jsp")
      matches("org/**/servlet/bla.jsp", "org/anyservlet/bla.jsp")
      matches("org/**", "org/anyservlet/bla.jsp")

      matchStart("org/**", "org/anyservlet/") should be(true)
      matchStart("org/**/*.jsp", "com/anyservlet") should be(false)
    }
  }
}
