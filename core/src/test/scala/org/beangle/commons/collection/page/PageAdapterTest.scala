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
package org.beangle.commons.collection.page
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
/**
 * @author zhouqi
 */
class PageAdapterTest extends FunSpec with ShouldMatchers {

  describe("PagedSeq") {
    it("Move next or previous") {
      val page = new PagedSeq[Int]((0 until 26).toList, 20)
      page.iterator.next should be(0)
      page.next()
      page.iterator.next should be(20)
      page.moveTo(2)
      page.iterator.next should be(20)
      page.previous()
      page.iterator.next should be(0)
    }
  }
}
