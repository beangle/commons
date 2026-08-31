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

package org.beangle.commons.aot

import org.beangle.commons.aot.AotPolicy.Category.*
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.testbean.TestEnum
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.nio.file.Files

object TestInitializer

class PlainInitializer

private val TestInitializerName = "org.beangle.commons.aot.TestInitializer"

class AotHintsTest extends AnyFunSpec, Matchers {

  class AotParent
  trait AotTrait
  class AotChild extends AotParent with AotTrait

  private def reflectEntries(hints: AotHints): Vector[JsonObject] = {
    val dir = Files.createTempDirectory("aot-hints")
    AotHintGenerator.write(dir, hints)
    val json = Files.readString(dir.resolve("reflect-config.json"), StandardCharsets.UTF_8)
    Json.parseArray(json).toVector.map(_.asInstanceOf[JsonObject])
  }

  describe("AotPolicy") {
    it("default is public methods + public constructors only") {
      AotPolicy.default.categories shouldBe Set(PublicMethods, PublicConstructors)
      AotPolicy.default.recursive shouldBe false
      AotPolicy.default.unsafeAllocated shouldBe false
    }

    it("merge unions categories and flags") {
      val a = AotPolicy(Set(PublicMethods, PublicConstructors))
      val b = AotPolicy(Set(DeclaredMethods, DeclaredFields), recursive = true, unsafeAllocated = true)
      a.merge(b) shouldBe AotPolicy(
        Set(PublicMethods, PublicConstructors, DeclaredMethods, DeclaredFields),
        recursive = true, unsafeAllocated = true)
    }
  }

  describe("AotHints.registerType") {
    it("default policy: no fields, no recursion, public members invocable") {
      val hints = new AotHints
      hints.registerType(classOf[AotChild])
      val entries = reflectEntries(hints)
      entries.map(e => e("name").toString) should contain only classOf[AotChild].getName
      val entry = entries.head
      entry("allPublicMethods") shouldBe true
      entry("allPublicConstructors") shouldBe true
      entry.get("allDeclaredMethods") shouldBe None
      entry.get("allDeclaredConstructors") shouldBe None
      entry.get("allDeclaredFields") shouldBe None
      entry.get("allPublicFields") shouldBe None
    }

    it("custom policy: declared members, fields, recursion expands hierarchy") {
      val hints = new AotHints
      val policy = AotPolicy(Set(DeclaredMethods, DeclaredConstructors, DeclaredFields), recursive = true)
      hints.registerType(classOf[AotChild], policy)
      val entries = reflectEntries(hints)
      entries.map(e => e("name").toString) should contain allOf (
        classOf[AotChild].getName, classOf[AotParent].getName, classOf[AotTrait].getName)
      entries foreach { e =>
        e("allDeclaredMethods") shouldBe true
        e("allDeclaredConstructors") shouldBe true
        e("allDeclaredFields") shouldBe true
      }
    }

    it("query categories emit queryAll flags without invoke flags") {
      val hints = new AotHints
      hints.registerType(classOf[AotChild], AotPolicy(Set(QueryPublicMethods, QueryPublicConstructors)))
      val entry = reflectEntries(hints).head
      entry("queryAllPublicMethods") shouldBe true
      entry("queryAllPublicConstructors") shouldBe true
      entry.get("allPublicMethods") shouldBe None
      entry.get("allPublicConstructors") shouldBe None
    }

    it("unsafeAllocated is emitted when requested") {
      val hints = new AotHints
      hints.registerType(classOf[AotChild], AotPolicy(Set(PublicMethods), unsafeAllocated = true))
      val entry = reflectEntries(hints).head
      entry("unsafeAllocated") shouldBe true
    }

    it("registering the same class twice merges policies") {
      val hints = new AotHints
      hints.registerType(classOf[AotChild])
      hints.registerType(classOf[AotChild], AotPolicy(Set(DeclaredFields)))
      val entry = reflectEntries(hints).head
      entry("allPublicMethods") shouldBe true
      entry("allDeclaredFields") shouldBe true
    }

    it("enum types register companion automatically with public fields") {
      val hints = new AotHints
      hints.registerType(classOf[TestEnum])
      val entries = reflectEntries(hints)
      entries.map(e => e("name").toString) should contain allOf (
        classOf[TestEnum].getName, classOf[TestEnum.type].getName)
      entries foreach { e =>
        e("allPublicMethods") shouldBe true
        e("allPublicConstructors") shouldBe true
        e("allPublicFields") shouldBe true
        e.get("allDeclaredFields") shouldBe None
      }
    }
  }

  describe("AotHints.registerArrayOf") {
    it("registers array type from simple class name with unsafeAllocated") {
      val hints = new AotHints
      hints.registerArrayOf("java.sql.Statement", getClass.getClassLoader)
      val entries = reflectEntries(hints)
      entries.map(e => e("name").toString) should contain only "[Ljava.sql.Statement;"
      entries.head("unsafeAllocated") shouldBe true
    }

    it("registers primitive arrays from simple type names") {
      val hints = new AotHints
      hints.registerArrayOf("int", getClass.getClassLoader)
      hints.registerArrayOf("boolean", getClass.getClassLoader)
      reflectEntries(hints).map(e => e("name").toString) should contain only ("[I", "[Z")
    }

    it("passes through descriptors and skips missing classes") {
      val hints = new AotHints
      hints.registerArrayOf("[Ljava.lang.String;", getClass.getClassLoader)
      hints.registerArrayOf("no.such.ArrayClass", getClass.getClassLoader)
      reflectEntries(hints).map(e => e("name").toString) should contain only "[Ljava.lang.String;"
    }
  }

  describe("resource patterns") {
    it("MetaAotHints registers beanmeta.idx pattern") {
      val registrar = new org.beangle.commons.bean.meta.MetaAotHints
      registrar.registering()
      registrar.aotHints.getPatterns should contain("META-INF/beangle/beanmeta.idx")
    }

    it("LogbackAotHints registers logback.xml pattern") {
      val registrar = new org.beangle.commons.logging.LogbackAotHints
      registrar.registering()
      registrar.aotHints.getPatterns should contain("logback\\.xml")
    }
  }

  describe("AotHintRegistrar") {
    it("hints use the overridable default policy") {
      val policy = AotPolicy(Set(DeclaredMethods, DeclaredConstructors, DeclaredFields), recursive = true)
      class CustomRegistrar extends AotHintRegistrar {
        override protected def aotPolicy: AotPolicy = policy
        override def registering(): Unit = hints.registerType(classOf[AotChild])
      }
      val registrar = new CustomRegistrar
      registrar.aotHints.policy shouldBe policy
      registrar.registering()
      val entries = reflectEntries(registrar.aotHints)
      entries.map(e => e("name").toString) should contain allOf (
        classOf[AotChild].getName, classOf[AotParent].getName, classOf[AotTrait].getName)
      entries.head("allDeclaredMethods") shouldBe true
    }
  }

  describe("registerClass") {
    it("registers an object initializer with its $ companion") {
      val hints = new AotHints
      val registered = AotHintGenerator.registerClass(TestInitializerName, getClass.getClassLoader, hints)
      registered shouldBe true
      val names = hints.getTypePolicies.keySet.map(_.getName)
      names should contain allOf (TestInitializerName, TestInitializerName + "$")
      hints.getTypePolicies(Class.forName(TestInitializerName)).categories shouldBe Set(PublicConstructors)
      hints.getTypePolicies(Class.forName(TestInitializerName + "$")).categories shouldBe
        Set(DeclaredConstructors, DeclaredFields)
    }

    it("registers a plain class without companion") {
      val hints = new AotHints
      val registered = AotHintGenerator.registerClass(
        classOf[PlainInitializer].getName, getClass.getClassLoader, hints)
      registered shouldBe true
      hints.getTypes.map(_.getName) should contain only classOf[PlainInitializer].getName
      hints.getTypePolicies(classOf[PlainInitializer]).categories shouldBe Set(PublicConstructors)
    }

    it("returns false for a missing class") {
      val hints = new AotHints
      AotHintGenerator.registerClass("org.beangle.commons.aot.NoSuchInitializer",
        getClass.getClassLoader, hints) shouldBe false
      hints.isEmpty shouldBe true
    }
  }
}
