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
package org.beangle.commons.orm

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.model.TestUser

/**
 * @author chaostone
 */
@RunWith(classOf[JUnitRunner])
class ProxyTest extends FunSpec with Matchers {
  describe("Proxy") {
    it("generate proxy") {
      val proxy1 = Proxy.generate(classOf[TestUser])
      val user1 = proxy1.asInstanceOf[TestUser]
      assert(user1.id == 0L)
      assert(null != user1.member)
      assert(null != user1.member.name)
      assert(user1.member.name.firstName == null)

      val accessed = proxy1.lastAccessed()
      assert(accessed.contains("id"))
      assert(accessed.contains("member.name.firstName"))

      val user2 = Proxy.generate(classOf[TestUser]).asInstanceOf[TestUser]
      assert(user2.member != user1.member)
    }
  }
}
