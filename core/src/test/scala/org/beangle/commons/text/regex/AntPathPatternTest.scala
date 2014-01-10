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
package org.beangle.commons.text.regex

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.beangle.commons.text.regex.AntPathPattern._

class AntPathPatternTest extends FunSpec with ShouldMatchers {

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
    }
  }
}
