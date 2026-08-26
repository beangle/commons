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

package org.beangle.commons.lang

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}

class ClassLoadersTest extends AnyFunSpec, Matchers {

  describe("ClassLoaders") {
    it("load primatives") {
      classOf[Int] should be(ClassLoaders.load("Int"))
      classOf[Int] should be(ClassLoaders.load("int"))
      classOf[Unit] should be(ClassLoaders.load("void"))
      classOf[Unit] should be(ClassLoaders.load("Unit"))
      classOf[Integer] should be(ClassLoaders.load("Integer"))

      ClassLoaders.getResource("/logback.xml").nonEmpty should be(true)
      ClassLoaders.getResource("logback.xml").nonEmpty should be(true)
    }
    it("returns None for missing classes") {
      ClassLoaders.get("no.such.Clazz") shouldBe None
    }
    it("returns None instead of throwing when a referenced type is missing") {
      // 模拟"classes 不全"：Child.class 存在但父类 Base.class 缺失（编译未完成）
      val dir = Files.createTempDirectory("classloaders-test")
      try {
        val base = dir.resolve("Base.java")
        Files.writeString(base, "public class Base {}\n")
        val child = dir.resolve("Child.java")
        Files.writeString(child, "public class Child extends Base {}\n")
        val result = javax.tools.ToolProvider.getSystemJavaCompiler()
          .run(null, null, null, "-d", dir.toString, base.toString, child.toString)
        result should be(0)
        Files.delete(dir.resolve("Base.class"))
        val loader = new java.net.URLClassLoader(Array(dir.toUri.toURL), getClass.getClassLoader)
        ClassLoaders.get("Child", loader) shouldBe None
      } finally {
        deleteRecursively(dir)
      }
    }
  }

  private def deleteRecursively(dir: Path): Unit = {
    if (Files.isDirectory(dir)) {
      val it = Files.list(dir)
      try it.forEach(p => deleteRecursively(p))
      finally it.close()
    }
    Files.deleteIfExists(dir)
  }
}
