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
import org.beangle.commons.io.BinarySerializer
import org.beangle.commons.jndi.JndiDataSourceFactory
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.testbean.{Book, Entity, Professor, TestChild2Bean}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}
import javax.sql.DataSource

class ReflectionsTest extends AnyFunSpec, Matchers {

  private def deleteRecursively(dir: Path): Unit = {
    if (Files.isDirectory(dir)) {
      val it = Files.list(dir)
      try it.forEach(p => deleteRecursively(p))
      finally it.close()
    }
    Files.deleteIfExists(dir)
  }

  describe("Reflections") {
    it("getSuperClassParamType") {
      val dataSourceType = Reflections.getGenericParamTypes(classOf[JndiDataSourceFactory], classOf[Factory[_]])
      assert(dataSourceType.size == 1)
      assert(dataSourceType.values.head == classOf[DataSource])

      val idType = Reflections.getGenericParamTypes(classOf[Book], classOf[Entity[_]])
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
      val atypes = Reflections.getGenericParamTypes(classOf[C], Set(classOf[A[_]]))
      assert(atypes.size == 1)
      assert(atypes.get("T").isDefined)

      val btypes = Reflections.getGenericParamTypes(classOf[C], classOf[B[_]])
      assert(btypes.size == 1)
      assert(btypes.contains("T1"))
    }
    it("newInstance") {
      val a: String = Reflections.newInstance("java.lang.String")
      assert(a.isEmpty)

      val i: Int = Reflections.newInstance(classOf[Int])
      assert(i == 0)

      val n: Any = Reflections.newInstance(classOf[Serializable])
      assert(n == null)

      val b: Any = Reflections.newInstance(classOf[BinarySerializer])
      assert(b == null)
    }
    it("getInstance") {
      val a = Reflections.getInstance[Co.type]("org.beangle.commons.lang.reflect.Co$")
      assert(a.isInstanceOf[Co.type])
    }
    it("tryGetInstance") {
      val co1 = Reflections.tryGetInstance[Co.type]("org.beangle.commons.lang.reflect.Co")
      assert(co1.contains(Co))
      val co2 = Reflections.tryGetInstance[Co.type]("org.beangle.commons.lang.reflect.Co$")
      assert(co2.contains(Co))
      assert(Reflections.tryGetInstance[String]("org.beangle.commons.lang.reflect.Co").isEmpty)

      assert(Reflections.tryGetInstance[String]("java.lang.String").contains(""))
      assert(Reflections.tryGetInstance[Integer]("java.lang.String").isEmpty)
      assert(Reflections.tryGetInstance[AnyRef]("java.lang.ProcessBuilder").isEmpty)
      assert(Reflections.tryGetInstance[AnyRef]("no.such.Class").isEmpty)
    }
    it("tryGetInstance swallows missing referenced types") {
      // 模拟"classes 不全"：Child.class 存在但父类 Base.class 缺失，不抛异常而是返回 None
      val dir = Files.createTempDirectory("reflections-test")
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
        Reflections.tryGetInstance[AnyRef]("Child", loader) shouldBe None
      } finally {
        deleteRecursively(dir)
      }
    }
    it("getField") {
      val p = new Professor(2L)
      p.depart = "r&d"
      val f = Reflections.getField(classOf[Professor], "depart")
      assert(f.nonEmpty)
      assert(f.get.get(p) == "r&d")
    }

  }
}

trait A[T]

trait B[T1] extends A[T1]

class C extends B[Integer]

object Co
