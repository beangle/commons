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

import org.beangle.commons.bean.meta.{MetaLoader, MetaModels}
import org.beangle.commons.lang.testbean.{Dog, QiutianDog}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** Bridge method tests.
  *
  * Bridge methods are generated when a subclass overrides a generic method with a
  * more specific type. For example:
  * {{{
  *   trait Animal { def getAge: Number }
  *   class Dog extends Animal { def getAge: Integer = 0 }
  * }}}
  * At bytecode level, Dog has two `getAge` methods:
  * - `public Integer getAge()` — the actual implementation
  * - `public Number getAge()` — bridge method (isBridge=true), delegates to the above
  */
class BridgeMethodTest extends AnyFunSpec, Matchers {

  describe("Bridge methods") {
    it("Dog.getAge has a bridge method returning Number") {
      val methods = classOf[Dog].getMethods.filter(_.getName == "getAge")
      assert(methods.length == 2)
      val bridge = methods.find(_.getReturnType == classOf[Number])
      val concrete = methods.find(_.getReturnType == classOf[Integer])
      assert(bridge.isDefined && bridge.get.isBridge)
      assert(concrete.isDefined && !concrete.get.isBridge)
    }

    it("MetaLoader prefers non-bridge getter") {
      val cm = MetaLoader.load(classOf[Dog])
      val ageProp = cm.properties.find(_.name == "age")
      assert(ageProp.isDefined, "age property should be discovered")
      // getterName should point to the method name (both bridge and concrete have same name)
      assert(ageProp.get.getterName.nonEmpty)
      // BeanInfo.from should resolve to the non-bridge method
      val bi = BeanInfo.from(cm)
      val getter = bi.getGetter("age")
      assert(getter.isDefined, "getter MethodHandle should be resolved")
      // Invoke: should return Integer (concrete), not Number (bridge)
      val dog = new Dog
      val result = getter.get.invoke(dog)
      assert(result.isInstanceOf[Integer])
    }

    it("getGetterMethod prefers non-bridge") {
      val bi = BeanInfos.register(MetaModels.of(classOf[Dog]))
      val method = bi.getGetterMethod("age")
      assert(method.isDefined)
      assert(!method.get.isBridge, "should prefer non-bridge method")
      assert(method.get.getReturnType == classOf[Integer])
    }

    it("Cannot find protected methods") {
      val bi = BeanInfos.register(MetaModels.of(classOf[QiutianDog]))
      assert(!bi.properties.contains("skills"))
    }
  }
}
