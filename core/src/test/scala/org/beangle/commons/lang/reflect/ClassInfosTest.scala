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
package org.beangle.commons.lang.reflect

import org.beangle.commons.lang.testbean.NumIdBean

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ClassInfosTest extends FunSpec with Matchers {

  describe("Entity") {
    it("transient persisted property") {
      val m = BeanInfos.get(classOf[NumIdBean[_]]).properties.get("persisted")
      assert(None != m)
      assert(m.get.isTransient)
      val mis = ClassInfos.get(classOf[NumIdBean[_]]).getMethods("persisted")
      assert(mis.size == 1)
      val mi = mis.head
      val anns = mi.method.getAnnotations
      assert(null != anns && anns.length == 1)
    }
  }
}
