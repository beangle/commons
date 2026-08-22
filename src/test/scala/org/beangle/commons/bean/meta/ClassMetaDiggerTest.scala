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

import org.beangle.commons.bean.meta.MetaModel.ClassMeta
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.{BeanInfos, TypeInfo}
import org.beangle.commons.lang.reflect.TypeInfo.IterableType
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ClassMetaDiggerTest extends AnyFunSpec, Matchers {
  ClassMetas.of(classOf[PersonMeta])
  ClassMetas.of(classOf[OtherMeta])

  describe("ClassMetas.of") {
    it("digs properties with types, transient and optional flags") {
      val cm = ClassMetas.of(classOf[PersonMeta])
      assert(cm.clazz == classOf[PersonMeta])
      val props = cm.properties.map(_.name)
      assert(props == Seq("age", "email", "name", "nickname", "parents", "scores", "tags", "type"))

      val byName = cm.properties.map(p => (p.name, p)).toMap
      assert(byName("name").typeinfo.clazz == classOf[String])
      assert(!byName("name").isTransient && !byName("name").isOptional)

      assert(byName("age").typeinfo.clazz == classOf[Int])
      assert(!byName("age").isTransient)

      assert(byName("email").typeinfo.clazz == classOf[String])
      assert(byName("email").isOptional) // Option[String] peeled to element

      assert(byName("nickname").typeinfo.clazz == classOf[String])
      assert(!byName("nickname").isTransient) // has setter

      assert(byName("parents").typeinfo.isInstanceOf[IterableType])
      assert(byName("parents").isTransient) // virtual getter, no setter, not in ctor

      assert(byName("scores").typeinfo.asInstanceOf[IterableType].isMap)
      assert(!byName("scores").isTransient)

      assert(byName("tags").typeinfo.asInstanceOf[IterableType].elementType.clazz == classOf[String])

      assert(byName("type").typeinfo.clazz == classOf[String]) // escaped key
    }

    it("digs constructors with defaults, primary first") {
      val cm = ClassMetas.of(classOf[PersonMeta])
      assert(cm.ctors.size == 2)
      val primary = cm.ctors.head
      assert(primary.parameters.map(_.name) == Seq("name", "age", "tags"))
      assert(primary.parameters(1).defaultValue.nonEmpty)
      assert(primary.parameters(2).defaultValue.nonEmpty)
      assert(cm.ctors(1).parameters.map(_.name) == Seq("name"))
    }

    it("digs methods with erased JVM param types") {
      val cm = ClassMetas.of(classOf[PersonMeta])
      val methods = cm.methods.map(m => (m.name, m.paramTypes)).toMap
      assert(methods.keySet == Set("greet", "childrenCount"))
      assert(methods("greet") == Seq("java/lang/String"))
      assert(methods("childrenCount") == Seq.empty)
      assert(!methods.contains("secret")) // noreflect
      assert(!methods.contains("copy")) // case class ignore
      assert(!methods.contains("canEqual"))
    }

    it("supports the varargs form") {
      val list = ClassMetas.of(classOf[PersonMeta], classOf[OtherMeta])
      assert(list.map(_.clazz) == Seq(classOf[PersonMeta], classOf[OtherMeta]))
    }

    it("round-trips through the v2 codec") {
      // defaults must be codec-supported types (List defaults are dropped by writeDefault)
      val cm = ClassMetas.of(classOf[CodecMeta])
      assert(MetaCodec.parse(MetaCodec.encode(cm)) == cm)
    }

    it("agrees with the runtime BeanInfo conversion path") {
      val macroCm = normalize(ClassMetas.of(classOf[PersonMeta]))
      val runtimeCm = normalize(BeanMetaConverter.from(BeanInfos.of(classOf[PersonMeta])))
      assert(macroCm == runtimeCm)
    }
  }

  /** Sorts methods for order-insensitive comparison (properties already sorted by name). */
  private def normalize(cm: ClassMeta): ClassMeta = cm.copy(methods = cm.methods.sortBy(m => (m.name, m.paramTypes.mkString("|"))))
}

case class PersonMeta(name: String, age: Int = 18, tags: List[String] = Nil) {
  var nickname: String = _
  var email: Option[String] = _
  var `type`: String = _
  var scores: Map[String, Int] = _

  def parents: List[String] = Nil

  @noreflect def secret(): String = "s"

  def childrenCount(): Int = 0

  def greet(prefix: String): String = prefix + name

  def this(name: String) = this(name, 18, Nil)
}

class OtherMeta {
  var code: Int = _
}

case class CodecMeta(name: String, size: Int = 10, ratio: Double = 0.5)
