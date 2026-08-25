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
import java.nio.file.Path

class MetaGeneratorTest extends AnyFunSpec, Matchers {

  describe("MetaGenerator") {
    it("scans classes directory and finds MetaRegistrar subclasses") {
      // Get the test-classes directory from classpath
      val resource = getClass.getResource("/")
      if (resource != null) {
        val classesDir = Path.of(resource.toURI)
        val metas = MetaGenerator.collectFromDirs(Seq(classesDir.toString))

        // Should find TestAppRegistry and collect GeneratorTestEntity
        metas.map(_.clazz.getName) should contain("org.beangle.commons.bean.meta.GeneratorTestEntity")
      }
    }

    it("generates beaninfo.idx to stream") {
      val resource = getClass.getResource("/")
      if (resource != null) {
        val classesDir = Path.of(resource.toURI)
        val metas = MetaGenerator.collectFromDirs(Seq(classesDir.toString))

        val out = new ByteArrayOutputStream()
        MetaIndex.write(out, metas)

        val bytes = out.toByteArray
        bytes.length should be > 0

        // Verify magic "BMXI"
        bytes(0) shouldBe 'B'
        bytes(1) shouldBe 'M'
        bytes(2) shouldBe 'X'
        bytes(3) shouldBe 'I'
      }
    }
  }
}

/** Test entity for generator testing. */
case class GeneratorTestEntity(id: Long, name: String, value: Int = 0)

/** Test registry for generator testing. */
class TestAppRegistry extends MetaRegistrar {
  register(classOf[GeneratorTestEntity])
}
