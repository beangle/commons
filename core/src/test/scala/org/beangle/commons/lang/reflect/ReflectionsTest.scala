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

import org.beangle.commons.bean.Factory
import org.beangle.commons.jndi.JndiDataSourceFactory
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.testbean.{ Book, TestChild2Bean }
import org.beangle.commons.lang.testbean.Entity
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.JUnitRunner
import javax.sql.DataSource

@RunWith(classOf[JUnitRunner])
class ReflectionsTest extends FunSpec with Matchers {

  describe("Reflections") {
    it("getSuperClassParamType") {
      val dataSourceType = Reflections.getGenericParamType(classOf[JndiDataSourceFactory], classOf[Factory[_]])
      assert(dataSourceType.size == 1)
      assert(dataSourceType.values.head == classOf[DataSource])

      val idType = Reflections.getGenericParamType(classOf[Book], classOf[Entity[_]])
      assert(idType.size == 1)
      assert(idType.values.head == classOf[java.lang.Long])
    }
    it("getAnnotation") {
      val clazz = classOf[TestChild2Bean]
      val method1 = clazz.getMethod("method1", classOf[Long])
      val method2 = clazz.getMethod("method2", classOf[Long])
      assert(null != Reflections.getAnnotation(method1, classOf[description]))
      assert(null == Reflections.getAnnotation(method2, classOf[description]))
    }
    it("getTraitParamType") {
      val atypes = Reflections.getGenericParamType(classOf[C], classOf[A[_]])
      assert(atypes.size == 1)
      assert(atypes.get("T").isDefined)

      val btypes = Reflections.getGenericParamType(classOf[C], classOf[B[_]])
      assert(btypes.size == 1)
      assert(btypes.get("T1").isDefined)
    }
  }
}

trait A[T]

trait B[T1] extends A[T1]

class C extends B[Integer]
