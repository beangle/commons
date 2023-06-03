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

package org.beangle.commons.lang.reflect

import org.beangle.commons.collection.Properties
import org.beangle.commons.lang.reflect.{BeanInfos, TypeInfo}
import org.beangle.commons.lang.testbean
import org.beangle.commons.lang.testbean.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.beans.{BeanInfo, Transient}
import java.lang.reflect.Modifier

class BeanInfoDiggerTest extends AnyFunSpec with Matchers {

  describe("BeanInfoDigger") {
    it("dig") {
      val a = new AA
      a.title()
      a.id = 3L
      //      println(BeanInfos.of(classOf[R]))
      //      println(BeanInfos.of(classOf[AA],classOf[TT]))
      //      println(BeanInfos.get(classOf[TT]))
      val d = BeanInfos.of(classOf[LongFactory])
      println(d)
    }
  }
}
