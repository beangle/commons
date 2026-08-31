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

import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.lang.annotation.noreflect
import org.beangle.commons.lang.reflect.{BeanInfo, BeanInfos, TypeInfo}
import org.beangle.commons.lang.reflect.TypeInfo.IterableType
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MetaDiggerTest extends AnyFunSpec, Matchers {
  MetaModels.of(classOf[PersonMeta])
  MetaModels.of(classOf[OtherMeta])

  describe("MetaModels.of") {
    it("digs properties with types, transient and optional flags") {
      val cm = MetaModels.of(classOf[PersonMeta])
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
      val cm = MetaModels.of(classOf[PersonMeta])
      assert(cm.ctors.size == 2)
      val primary = cm.ctors.head
      assert(primary.parameters.map(_.name) == Seq("name", "age", "tags"))
      assert(primary.parameters(1).defaultValue.nonEmpty)
      assert(primary.parameters(2).defaultValue.nonEmpty)
      assert(cm.ctors(1).parameters.map(_.name) == Seq("name"))
    }

    it("supports the varargs form") {
      val list = MetaModels.of(classOf[PersonMeta], classOf[OtherMeta])
      assert(list.map(_.clazz) == Seq(classOf[PersonMeta], classOf[OtherMeta]))
    }

    it("digs class literals passed through inline wrappers") {
      inline def digOne(inline c: Class[_]): List[BeanMeta] = MetaModels.of(c)
      val list = digOne(classOf[PersonMeta])
      assert(list.map(_.clazz) == Seq(classOf[PersonMeta]))
    }

    it("digs JDK classes into an empty BeanMeta without crashing") {
      val cm = MetaModels.of(classOf[java.lang.String])
      assert(cm.clazz == classOf[java.lang.String])
      assert(cm.properties.isEmpty && cm.ctors.isEmpty)

      val alias = MetaModels.of(classOf[String])
      assert(alias.clazz == classOf[String])
      assert(alias.properties.isEmpty && alias.ctors.isEmpty)
    }

    it("round-trips through the v2 codec") {
      // defaults must be codec-supported types (List defaults are dropped by writeDefault)
      val cm = MetaModels.of(classOf[CodecMeta])
      assert(MetaCodec.parse(MetaCodec.encode(cm)) == cm)
    }

    it("round-trips PersonMeta properties through the v2 codec") {
      val cm = MetaModels.of(classOf[PersonMeta])
      val parsed = MetaCodec.parse(MetaCodec.encode(cm))
      assert(parsed.properties == cm.properties)
      assert(parsed.clazz == cm.clazz)
    }
  }

  describe("getterName for var name plus override def getName") {
    it("compile-time dig keeps the field-backed getter name") {
      val cm = MetaModels.of(classOf[GetterNameMeta])
      val p = cm.properties.find(_.name == "name").get
      info(s"beanmeta getterName = ${p.getterName}")
      assert(p.getterName == "name")
    }

    it("runtime reflection keeps the field-backed getter name") {
      val cm = MetaLoader.load(classOf[GetterNameMeta])
      val p = cm.properties.find(_.name == "name").get
      info(s"runtime getterName = ${p.getterName}")
      assert(p.getterName == "name")
    }
  }

  describe("getterName for var writable plus override def isWritable") {
    it("compile-time dig keeps the field-backed getter name") {
      val cm = MetaModels.of(classOf[IsWritableMeta])
      val p = cm.properties.find(_.name == "writable").get
      info(s"beanmeta getterName = ${p.getterName}")
      assert(p.getterName == "writable")
    }

    it("runtime reflection keeps the field-backed getter name") {
      val cm = MetaLoader.load(classOf[IsWritableMeta])
      val p = cm.properties.find(_.name == "writable").get
      info(s"runtime getterName = ${p.getterName}")
      assert(p.getterName == "writable")
    }

    it("getter is invocable via BeanInfos") {
      val bi = BeanInfos.register(MetaModels.of(classOf[IsWritableMeta]))
      val getter = bi.getGetter("writable")
      assert(getter.isDefined)
      val m = new IsWritableMeta
      m.writable = true
      assert(getter.get.invoke(m).asInstanceOf[Boolean])
    }
  }

  describe("plain java bean") {
    it("MetaDig digs plain java bean properties following the JavaBean convention") {
      val cm = MetaModels.of(classOf[PlainJavaBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      info(s"java bean properties = ${byName.keySet.toSeq.sorted}")
      assert(byName.keySet == Set("name", "age", "writable"))
      assert(byName("name").getterName == "getName")
      assert(byName("name").setterName.contains("setName"))
      assert(byName("age").getterName == "getAge")
      assert(byName("age").setterName.contains("setAge"))
      assert(byName("writable").getterName == "isWritable")
      assert(byName("writable").setterName.contains("setWritable"))
    }

    it("MetaLoader digs java bean properties with getterName") {
      val cm = MetaLoader.load(classOf[PlainJavaBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      info(s"java bean properties = ${byName.keySet.toSeq.sorted}")
      assert(byName.keySet == Set("name", "age", "writable"))
      assert(byName("name").getterName == "getName")
      assert(byName("name").setterName.contains("setName"))
      assert(byName("age").getterName == "getAge")
      assert(byName("age").setterName.contains("setAge"))
      assert(byName("writable").getterName == "isWritable")
      assert(byName("writable").setterName.contains("setWritable"))
    }
  }

  describe("write-only java bean") {
    it("MetaLoader keeps BeanMeta.properties readable-only, setter-only properties excluded") {
      val cm = MetaLoader.load(classOf[WriteOnlyBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      assert(byName.keySet == Set("name"))
      assert(byName("name").getterName == "getName")
      assert(byName("name").setterName.contains("setName"))
    }

    it("BeanInfos collects write-only properties into writeOnlys") {
      val bi = BeanInfos.get(classOf[WriteOnlyBean])
      info(s"writeOnlys = ${bi.writeOnlys}")
      assert(bi.writeOnlys.keySet == Set("secret", "enabled", "proxyInterfaces"))
      assert(bi.writeOnlys("secret").getName == "setSecret")
      assert(bi.writeOnlys("enabled").getName == "setEnabled")
      assert(bi.writeOnlys("proxyInterfaces").getName == "setProxyInterfaces")
      assert(!bi.properties.contains("secret"))
      assert(bi.getGetter("secret").isEmpty)
      assert(bi.getSetterMethod("secret").map(_.getName).contains("setSecret"))
      assert(bi.getSetterMethod("enabled").map(_.getName).contains("setEnabled"))
      assert(!bi.writeOnlys.contains("name"))
    }

    it("scala subclass inherits write-only setters like TransactionProxyFactoryBean") {
      val bi = BeanInfos.get(classOf[WriteOnlySubBean])
      info(s"writeOnlys = ${bi.writeOnlys}")
      assert(bi.writeOnlys.keySet == Set("secret", "enabled", "proxyInterfaces"))
      assert(bi.writeOnlys("proxyInterfaces").getName == "setProxyInterfaces")
      assert(bi.getSetterMethod("proxyInterfaces").isDefined)
    }

    it("BeanInfo.from enriches readable JavaBean properties missing from the meta") {
      val full = MetaLoader.load(classOf[PlainJavaBean])
      val partial = full.copy(properties = Seq(full.properties.find(_.name == "name").get))
      val bi = BeanInfo.from(partial)
      info(s"enriched props = ${bi.properties.keySet.toSeq.sorted}")
      assert(bi.properties.keySet == Set("name", "age", "writable"))
      assert(bi.properties("age").setter.isDefined)
      assert(bi.writeOnlys.isEmpty)
    }
  }

  describe("inherited readable java properties (partial meta into idx)") {
    it("MetaDig merges readable JavaBean properties from java parents into BeanMeta") {
      val cm = MetaModels.of(classOf[ScalaChildBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      info(s"BeanMeta properties = ${byName.keySet.toSeq.sorted}")
      assert(byName.keySet == Set("id", "title"))
      assert(byName("id").getterName == "id")
      assert(byName("title").getterName == "getTitle")
      assert(byName("title").setterName.contains("setTitle"))
    }

    it("BeanInfo.from resolves merged inherited getters and setters") {
      val cm = MetaModels.of(classOf[ScalaChildBean])
      val bi = BeanInfo.from(cm)
      info(s"BeanInfo properties = ${bi.properties.keySet.toSeq.sorted}")
      assert(bi.properties.keySet == Set("id", "title"))
      assert(bi.getGetter("title").isDefined)
      assert(bi.getSetter("title").isDefined)
      val s = new ScalaChildBean
      s.setTitle("t")
      assert(bi.getGetter("title").get.invoke(s) == "t")
      assert(bi.getSetter("id").isDefined)
    }

    it("BeanMeta of write-only subclass keeps readable parent property, write-only stays excluded") {
      val cm = MetaModels.of(classOf[WriteOnlySubBean])
      val names = cm.properties.map(_.name)
      info(s"BeanMeta properties = $names")
      assert(names == Seq("name"))
      val bi = BeanInfo.from(cm)
      assert(bi.writeOnlys.keySet == Set("secret", "enabled", "proxyInterfaces"))
      assert(bi.getGetter("name").isDefined)
    }
  }

  describe("virtual properties (def x; def x_=)") {
    it("digs setter-first virtual property pair") {
      val cm = MetaModels.of(classOf[VirtualPropsMeta])
      val p = cm.properties.find(_.name == "base").get
      assert(p.getterName == "base")
      assert(p.setterName.contains("base_$eq"))
      assert(!p.isTransient)
    }

    it("digs getter-first virtual property pair") {
      val cm = MetaModels.of(classOf[VirtualPropsMeta2])
      val p = cm.properties.find(_.name == "base").get
      assert(p.getterName == "base")
      assert(p.setterName.contains("base_$eq"))
      assert(!p.isTransient)
    }

    it("virtual property getter/setter are invocable via BeanInfo") {
      val cm = MetaModels.of(classOf[VirtualPropsMeta])
      val bi = BeanInfo.from(cm)
      val m = new VirtualPropsMeta
      bi.getSetter("base").get.invoke(m, "hello")
      assert(bi.getGetter("base").get.invoke(m) == "hello")
    }
  }

  describe("singleton-typed property (TermRef)") {
    it("digs a TermRef property by widening to its underlying class") {
      val cm = MetaModels.of(classOf[TermRefMeta])
      val p = cm.properties.find(_.name == "math").get
      info(s"math typeinfo = ${p.typeinfo}")
      assert(p.typeinfo.clazz == MathOpsMeta.getClass)
    }
  }

  describe("module class (object) dig") {
    it("digs classOf[ModuleWithNested.type] without nested-class module fields") {
      val cm = MetaModels.of(classOf[ModuleWithNested.type])
      assert(cm.clazz == ModuleWithNested.getClass)
      assert(cm.properties.map(_.name) == Seq("value"))
    }
  }
}

/** Scala 子类继承 Java 父类的 write-only setter（对应 Spring TransactionProxyFactoryBean 场景）。 */
class WriteOnlySubBean extends WriteOnlyBean

/** Scala 子类继承 Java 父类：BeanMeta 应合并父类可读属性（进入 beanmeta.idx）。 */
class ScalaChildBean extends JavaParentBean {
  var id: Long = _
}

/** 虚拟属性（无字段）：setter 先于 getter 声明，验证 MetaDigger 发现顺序无关。 */
class VirtualPropsMeta {
  private var _base: String = _
  def base_=(n: String): Unit = _base = n
  def base: String = _base
}

/** 虚拟属性（无字段）：getter 先于 setter 声明，对照用例。 */
class VirtualPropsMeta2 {
  private var _base: String = _
  def base: String = _base
  def base_=(n: String): Unit = _base = n
}

/** 单例（TermRef）类型属性：`def math: MathOpsMeta.type = MathOpsMeta`，
 *  回归 MetaDigger 对 TermRef 的解析（此前会直接抛 Unsupported type）。 */
object MathOpsMeta {
  def ceil(n: java.lang.Number): Int = n.intValue
}

class TermRefMeta {
  def math: MathOpsMeta.type = MathOpsMeta
}

/** var name 与 JavaBean 风格 getName 叠加：字段访问器优先，beanmeta（编译期 MetaDigger）
 * 与运行期 MetaLoader 的 getterName 保持一致，用于固定该契约。 */
class GetterNameMeta {
  var name: String = _
  def getName: String = name
}

/** var writable 与 JavaBean 风格 isWritable 叠加（boolean 属性）：同样字段访问器优先。 */
class IsWritableMeta {
  var writable: Boolean = _
  def isWritable: Boolean = writable
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


/** 模块类（object）dig：TASTy 把嵌套类/对象表示为合成 lazy val 模块字段
 *  （Flags.Module），不得作为 bean 属性（其单例类型运行期会引用不存在的 `$` 伴生类）。 */
object ModuleWithNested {
  class Nested1
  class Nested2
  var value: String = _
}

class ModuleWithNestedUser {
  def module: ModuleWithNested.type = ModuleWithNested
}
