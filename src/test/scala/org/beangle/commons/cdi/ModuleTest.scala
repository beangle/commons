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

package org.beangle.commons.cdi

import org.beangle.commons.config.Environment
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ModuleTest extends AnyFunSpec, Matchers {

  describe("BindModule") {
    it("has corrent dev model detected.") {
      val module = new BindModule {
        protected override def binding(): Unit = {}
      }
      System.setProperty(Environment.ProfileKey, "dev")
      assert(module.devEnabled)
      System.clearProperty(Environment.ProfileKey)
      assert(!module.devEnabled)
      System.setProperty(Environment.ProfileKey, "dev,other")
      assert(module.devEnabled)
    }

    it("registers BeanMeta when binder is null (compile-time dig)") {
      val module = new TestBindModule
      module.registering()
      assert(module.metas.map(_.clazz).toSet == Set(classOf[BindModuleEntity], classOf[BindModuleInner], classOf[BindModuleExtra]))
    }
  }
}

class BindModuleEntity {
  var name: String = _
  var age: Int = _
}

class BindModuleInner {
  var code: String = _
}

class BindModuleExtra {
  var enabled: Boolean = _
}

class TestBindModule extends BindModule {
  protected override def binding(): Unit = {
    bind("demo", classOf[BindModuleEntity])
    bean(classOf[BindModuleInner])
    bind(classOf[BindModuleExtra])
  }
}
