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

import org.beangle.commons.bean.Factory
import org.beangle.commons.jndi.JndiDataSourceFactory
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.testbean.{ Book, Entity, TestChild2Bean,TestEnum }
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.beangle.commons.collection.Properties

import javax.sql.DataSource
import scala.collection.mutable

class TypeInfoTest extends AnyFunSpec with Matchers {
  describe("TypeInfo") {
    it("naming") {
      assert("Option[Int]" == TypeInfo.get(classOf[Int], true).name)
      assert("scala.collection.mutable.Buffer[String]" == TypeInfo.get(classOf[mutable.Buffer[_]], classOf[String]).name)
      assert("scala.collection.mutable.HashMap[String,Int]" == TypeInfo.get(classOf[mutable.HashMap[_, _]], classOf[String], classOf[Int]).name)

      assert("Array[Int]"== TypeInfo.get(classOf[Array[Int]],false).name)
      assert("Option[Array[Int]]"== TypeInfo.get(classOf[Array[Int]],true).name)
      assert("org.beangle.commons.collection.Properties[String,Object]" == TypeInfo.get(classOf[Properties]).name)

      assert("org.beangle.commons.lang.reflect.Props[java.lang.Long,String]" == TypeInfo.get(classOf[Props]).name)
      assert("org.beangle.commons.lang.reflect.D[String]" == TypeInfo.get(classOf[D]).name)
    }
  }
}