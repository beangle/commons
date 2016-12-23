/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.template.freemarker

import org.beangle.commons.collection.page.SinglePage
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ObjectWrapperTest extends FunSpec with Matchers {
  describe("BeangleObjectWrapper") {
    it("wrapper") {
      val wrapper = new BeangleObjectWrapper()
      val page = new SinglePage(2, 2, 100, List(21, 21))
      val wrapped = wrapper.wrap(page)
      val unwrapped = wrapper.unwrap(wrapped)
      assert(unwrapped == page)
    }
  }
}
