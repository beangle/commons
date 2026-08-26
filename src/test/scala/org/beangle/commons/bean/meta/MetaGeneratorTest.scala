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
import java.net.{JarURLConnection, URI}
import java.nio.file.{Files, Path, StandardCopyOption}

class MetaGeneratorTest extends AnyFunSpec, Matchers {

  /** Locates the test classes: a directory on the classpath, or a jar bundle
   * (e.g. sbt's cached test classes) extracted to a temp directory.
   */
  private def testClassesRoot: Path = {
    val loc = getClass.getProtectionDomain.getCodeSource.getLocation
    if loc.toString.startsWith("file:") && Files.isDirectory(Path.of(loc.toURI)) then Path.of(loc.toURI)
    else {
      val jarUri = if loc.toString.startsWith("jar:") then loc.toURI else URI.create("jar:" + loc.toURI + "!/")
      val conn = jarUri.toURL.openConnection().asInstanceOf[JarURLConnection]
      val dir = Files.createTempDirectory("test-classes")
      val it = conn.getJarFile.entries()
      while it.hasMoreElements do
        val e = it.nextElement()
        if !e.isDirectory then
          val target = dir.resolve(e.getName)
          Files.createDirectories(target.getParent)
          Files.copy(conn.getJarFile.getInputStream(e), target, StandardCopyOption.REPLACE_EXISTING)
      dir
    }
  }

  describe("MetaGenerator") {
    it("loads declared MetaRegistrar classes and collects BeanMeta") {
      val classesDir = testClassesRoot
      val metas = MetaGenerator.collectMetas(
        Seq("org.beangle.commons.bean.meta.TestAppRegistry"), Seq(classesDir.toString))

      // Should load TestAppRegistry and collect GeneratorTestEntity
      metas.map(_.clazz.getName) should contain("org.beangle.commons.bean.meta.GeneratorTestEntity")
    }

    it("generates beaninfo.idx to stream") {
      val classesDir = testClassesRoot
      val metas = MetaGenerator.collectMetas(
        Seq("org.beangle.commons.bean.meta.TestAppRegistry"), Seq(classesDir.toString))

      val out = new ByteArrayOutputStream()
      MetaIndex.write(out, metas)

      val bytes = out.toByteArray
      bytes.length should be > 0

      // Verify magic "BBXI"
      bytes(0) shouldBe 'B'
      bytes(1) shouldBe 'B'
      bytes(2) shouldBe 'X'
      bytes(3) shouldBe 'I'
    }
  }
}

/** Test entity for generator testing. */
case class GeneratorTestEntity(id: Long, name: String, value: Int = 0)

/** Test registry for generator testing. */
class TestAppRegistry extends MetaRegistrar {

  override def registering(): Unit = {
    register(classOf[GeneratorTestEntity])
  }
}
