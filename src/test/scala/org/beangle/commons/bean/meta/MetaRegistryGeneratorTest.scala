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

package org.beangle.commons.bean.meta

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayOutputStream
import java.nio.file.Files

class MetaRegistryGeneratorTest extends AnyFunSpec, Matchers {

  describe("MetaRegistryGenerator") {
    it("collects ClassMeta from specified registry class") {
      val metas = MetaRegistryGenerator.collectAll(Seq(classOf[TestAppRegistry].getName))
      metas should not be empty
      metas.map(_.clazz.getName) should contain("org.beangle.commons.bean.meta.GeneratorTestEntity")
    }

    it("generates beaninfo.idx to stream") {
      val metas = MetaRegistryGenerator.collectAll(Seq(classOf[TestAppRegistry].getName))
      val out = new ByteArrayOutputStream()
      MetaIndex.write(out, metas)

      val bytes = out.toByteArray
      bytes.length should be > 0

      // Verify magic
      bytes(0) shouldBe 'B'
      bytes(1) shouldBe 'N'
      bytes(2) shouldBe 'I'
      bytes(3) shouldBe 'X'
    }
  }
}

/** Test entity for generator testing. */
case class GeneratorTestEntity(id: Long, name: String, value: Int = 0)

/** Test registry for generator testing. */
class TestAppRegistry extends MetaRegistry {
  override protected def registering(): Unit = {
    register(classOf[GeneratorTestEntity])
  }
}
