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

import org.beangle.commons.lang.reflect.BeanInfo

import java.nio.file.Files

/** GraalVM native-image integration test.
  *
  * This test verifies that MetaGenerator produces valid config files
  * and BeanInfo read/write works correctly.
  */
class GraalVMIntegrationTest extends AnyFunSpec, Matchers {

  describe("GraalVM Integration") {
    it("MetaGenerator generates valid configs") {
      val classesDir = testClassesDir
      val metas = MetaGenerator.collectFromDirs(Seq(classesDir))
      metas should not be empty

      // Generate beanmeta.idx
      val tempDir = Files.createTempDirectory("graalvm-test")
      val outputPath = tempDir.resolve("beanmeta.idx")
      val out = new java.io.FileOutputStream(outputPath.toFile)
      try MetaIndex.write(out, metas)
      finally out.close()

      // Verify beanmeta.idx
      Files.exists(outputPath) shouldBe true
      val bytes = Files.readAllBytes(outputPath)
      bytes.length should be > 0

      // Cleanup
      Files.deleteIfExists(outputPath)
      Files.deleteIfExists(tempDir)
    }

    it("BeanInfo read/write roundtrip") {
      val classesDir = testClassesDir
      val metas = MetaGenerator.collectFromDirs(Seq(classesDir))
      metas should not be empty

      // Encode to binary
      val baos = new java.io.ByteArrayOutputStream()
      MetaIndex.write(baos, metas)
      val binaryData = baos.toByteArray

      // Decode from binary
      val bais = new java.io.ByteArrayInputStream(binaryData)
      val decodedMetas = MetaIndex.read(bais)

      decodedMetas.size shouldBe metas.size

      // Verify each class can reconstruct BeanInfo
      decodedMetas.foreach { cm =>
        val bi = BeanInfo.from(cm)
        bi should not be null
        bi.meta shouldBe cm
        bi.properties.size shouldBe cm.properties.size
      }
    }

    it("BeanInfo accessor invocation") {
      // Use compile-time dig to get BeanMeta
      val cm = MetaModels.of(classOf[GeneratorTestEntity])

      // Reconstruct BeanInfo
      val bi = BeanInfo.from(cm)

      // Verify we can get getter/setter
      bi.getGetter("id") shouldBe defined
      bi.getGetter("name") shouldBe defined

      // Test MethodHandle invocation
      val entity = new GeneratorTestEntity(1L, "test", 42)
      val idGetter = bi.getGetter("id").get
      val idResult = idGetter.invoke(entity).asInstanceOf[Long]
      idResult shouldBe 1L

      val nameGetter = bi.getGetter("name").get
      val nameResult = nameGetter.invoke(entity).asInstanceOf[String]
      nameResult shouldBe "test"
    }

    it("JVM.isGraal returns false on standard JVM") {
      org.beangle.commons.lang.JVM.isGraal shouldBe false
    }
  }

  private def testClassesDir: String = {
    val resource = getClass.getResource("/")
    if (resource != null && !resource.toString.contains(".jar"))
      resource.getPath
    else
      "target/out/jvm/u/beangle-commons/test-classes"
  }
}
