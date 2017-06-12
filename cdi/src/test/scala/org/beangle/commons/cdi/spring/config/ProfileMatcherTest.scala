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

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Chars

@RunWith(classOf[JUnitRunner])
class ProfileMatcherTest extends FunSpec with Matchers {

  describe("Profile Matcher") {
    it("match success") {
      val matcher = new ProfileMatcher(" school1(dev ,test (d,d2)), school2 ")
      assert(matcher.matches("school2"))
      assert(!matcher.matches("school1 "))
      assert(matcher.matches("school1 , dev"))
      assert(!matcher.matches("school1, test"))
      assert(matcher.matches("school1, test, d2"))
      
      val matcher2= new ProfileMatcher("production,dev")
      assert(matcher2.matches("dev"))
      assert(!matcher2.matches("productor"))
    }
  }
}
