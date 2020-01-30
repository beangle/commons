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
package org.beangle.commons.collection.page
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

/**
 * @author zhouqi
 */
@RunWith(classOf[JUnitRunner])
class PageAdapterTest extends AnyFunSpec with Matchers {

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
