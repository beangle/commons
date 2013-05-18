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
package org.beangle.commons.text.regex

import org.testng.Assert
import org.testng.annotations.Test
import org.beangle.commons.text.regex.AntPathPattern._
//remove if not needed
import scala.collection.JavaConversions._

@Test
class AntPathPatternTest {

  def test() {
    `match`("com/t?st.jsp", "com/test.jsp")
    `match`("com/*.jsp", "com/test.jsp")
    `match`("com/*.jsp", "com/dir/test.jsp")
    `match`("com/**/test.jsp", "com/dir1/dir2/test.jsp")
    `match`("com/beangle/**/*.jsp", "com/beangle/dir1/dir2/test3.jsp")
    `match`("org/**/servlet/bla.jsp", "org/beangle/servlet/bla.jsp")
    `match`("org/**/servlet/bla.jsp", "org/beangle/testing/servlet/bla.jsp")
    `match`("org/**/servlet/bla.jsp", "org/servlet/bla.jsp")
    `match`("org/**/servlet/bla.jsp", "org/anyservlet/bla.jsp")
    `match`("org/**", "org/anyservlet/bla.jsp")
  }
}
