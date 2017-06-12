/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.cdi.spring.config

import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.regex.AntPathPattern
import org.scalatest.{ FunSpec, Matchers }
import org.springframework.util.AntPathMatcher
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AntPathMatcherTest extends FunSpec with Matchers with Logging {

  describe("Beangle Ant Path Matcher") {
    it("match regex pattern") {
      val matcher = AntPathPattern

      matcher.matches("com/t?st.jsp", "com/test.jsp") should be(true)
      matcher.matches("com/*.jsp", "com/test.jsp") should be(true)
      matcher.matches("com/*.jsp", "com/dir/test.jsp") should be(false)

      matcher.matches("com/**/test.jsp", "com/dir1/dir2/test.jsp") should be(true)
      matcher.matches("com/beangle/**/*.jsp", "com/beangle/dir1/dir2/test3.jsp") should be(true)
      matcher.matches("org/**/servlet/bla.jsp", "org/beangle/servlet/bla.jsp") should be(true)
      matcher.matches("org/**/servlet/bla.jsp", "org/beangle/testing/servlet/bla.jsp") should be(true)
      matcher.matches("org/**/servlet/bla.jsp", "org/servlet/bla.jsp") should be(true)

      matcher.matches("org/**/servlet/bla.jsp", "org/anyservlet/bla.jsp") should be(false)
      matcher.matches("org/**", "org/anyservlet/bla.jsp") should be(true)
    }
  }

  describe("Pattern match benchmark") {
    val n=100
    it("Spring performance") {
      val matcher = new AntPathMatcher()
      val sw = new Stopwatch(true)
      var i = 0
      while (i < n) {
        matcher.`match`("org/**/servlet/bla.jsp", "org/beangle/servlet/bla.jsp")
        i += 1
      }
      logger.info(s"Spring AntPathMatcher $n's match using $sw")
    }
    it("Beangle performance") {
      val pattern = new AntPathPattern("org/**/servlet/bla.jsp");
      val sw = new Stopwatch(true);
      var i = 0
      while (i < n) {
        pattern.matches("org/beangle/servlet/bla.jsp")
        i += 1
      }
      logger.info(s"Beangle AntPattern $n's match using $sw")
    }
  }
}