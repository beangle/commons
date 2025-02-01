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

package org.beangle.commons.regex

import org.beangle.commons.regex.AntPathPattern.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class AntPathPatternTest extends AnyFunSpec, Matchers {

  describe("AntPathPattern") {
    it("Match ant pattern expression") {
      matches("com/t?st.jsp", "com/test.jsp") should be(true)
      matches("com/*.jsp", "com/test.jsp") should be(true)
      matches("com/**/*.jsp", "com/dir/test.jsp") should be(true)
      matches("com/*.jsp", "com/dir/test.jsp") should be(false)
      matches("com/**/test.jsp", "com/dir1/dir2/test.jsp") should be(true)
      matches("com/**/test.jsp", "com/test.jsp") should be(true)
      matches("com/beangle/**/*.jsp", "com/beangle/dir1/dir2/test3.jsp") should be(true)
      matches("org/**/servlet/bla.jsp", "org/beangle/servlet/bla.jsp") should be(true)
      matches("org/**/servlet/bla.jsp", "org/beangle/testing/servlet/bla.jsp") should be(true)
      matches("org/**/servlet/bla.jsp", "org/servlet/bla.jsp") should be(true)
      matches("org/**/servlet/bla.jsp", "org/anyservlet/bla.jsp") should be(false)
      matches("org/**", "org/anyservlet/bla.jsp") should be(true)
      matches("org/{id:[0-9]+}", "org/s") should be(false)
      matches("org/{[0-9]+}", "org/12323") should be(true)

      matchStart("org/**", "org/anyservlet/") should be(true)
      matchStart("org/**/*.jsp", "com/anyservlet") should be(false)
    }
  }
}
