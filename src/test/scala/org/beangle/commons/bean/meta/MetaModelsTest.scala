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

import org.beangle.commons.lang.reflect.BeanInfos
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.FileOutputStream
import java.nio.file.Files

class MetaModelsTest extends AnyFunSpec, Matchers {

  describe("MetaModels") {
    it("returns None for unregistered class") {
      MetaModels.get(classOf[String]) shouldBe None
      MetaModels.get("java.lang.String") shouldBe None
    }

    it("returns None for unregistered class name") {
      MetaModels.get("com.example.NonExistent") shouldBe None
    }

    it("contains returns false for unregistered class") {
      MetaModels.contains(classOf[String]) shouldBe false
      MetaModels.contains("java.lang.String") shouldBe false
    }

    it("classNames returns empty set when no idx files exist") {
      val names = MetaModels.classNames
      names shouldBe a[Set[_]]
    }

    it("loads BeanMeta from metamodel.idx via MetaIndex") {
      val metas = Seq(
        MetaModels.of(classOf[TestUser]),
        MetaModels.of(classOf[TestRole]))

      val tmpDir = Files.createTempDirectory("metamodel-test")
      val idxPath = tmpDir.resolve("metamodel.idx")
      MetaIndex.write(idxPath, metas)

      Files.exists(idxPath) shouldBe true

      val loaded = MetaIndex.read(idxPath)
      loaded.size shouldBe 2
      loaded.map(_.clazz) should contain allOf (classOf[TestUser], classOf[TestRole])

      loaded.find(_.clazz == classOf[TestUser]).foreach { cm =>
        cm.properties.map(_.name) should contain allOf ("id", "name")
      }

      Files.deleteIfExists(idxPath)
      Files.deleteIfExists(tmpDir)
    }
  }
}

class TestUser(var id: Long, var name: String)
class TestRole(var code: String)
