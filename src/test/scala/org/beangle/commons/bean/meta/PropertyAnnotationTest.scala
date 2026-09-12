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

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.page.{Page, PagedSeq, SinglePage}
import org.beangle.commons.lang.annotation.{noreflect, property}
import org.beangle.commons.lang.reflect.BeanInfo
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** `@property` 显式属性：MetaLoader（strict，JVM）/MetaLoaderLite（native）/MetaDigger（编译期 dig）
  * 三者的识别与命名必须一致：value 为空取方法名，非空取 value；只认参数个数为 0 的方法。
  */
class PropertyAnnotationTest extends AnyFunSpec, Matchers {

  private def byName(cm: MetaModel.BeanMeta): Map[String, MetaModel.Property] =
    cm.properties.map(p => (p.name, p)).toMap

  private def pairs(cm: MetaModel.BeanMeta): Map[String, String] =
    cm.properties.map(p => (p.name, p.getterName)).toMap

  describe("MetaLoader @property") {
    it("turns an annotated parameterless def into a property named by the method") {
      val props = byName(MetaLoader.load(classOf[AnnotatedBean]))
      props should contain key "hasPrevious"
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("hasPrevious").setterName shouldBe None
      props("hasPrevious").typeinfo.clazz shouldBe classOf[Boolean]
    }

    it("uses annotation value as property name and keeps the method name as getter") {
      val props = byName(MetaLoader.load(classOf[AnnotatedBean]))
      props should contain key "title"
      props("title").getterName shouldBe "displayName"
      props should not contain key("displayName")
    }

    it("ignores @property on methods with parameters and keeps setter convention") {
      val props = byName(MetaLoader.load(classOf[AnnotatedBean]))
      props should not contain key("viaSetter")
      props("tag").getterName shouldBe "tag"
      props("tag").setterName should contain("setTag")
      props should not contain key("setTag")
    }

    it("keeps unannotated parameterless defs out of properties") {
      byName(MetaLoader.load(classOf[AnnotatedBean])) should not contain key("plain")
    }

    it("ignores @property on zero-arg methods returning Unit") {
      val props = byName(MetaLoader.load(classOf[AnnotatedBean]))
      props should not contain key("noop")
      props should not contain key("silent")
    }

    it("ignores @noreflect methods even when annotated with @property") {
      byName(MetaLoader.load(classOf[AnnotatedBean])) should not contain key("hidden")
    }

    it("exposes annotated SinglePage properties on the strict loader") {
      val props = byName(MetaLoader.load(classOf[SinglePage[String]]))
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("hasNext").getterName shouldBe "hasNext"
      props("length").getterName shouldBe "length"
      props("totalPages").getterName shouldBe "totalPages"
      // 未注解的参数less def 仍不识别（字节码上无法与空参方法区分）
      props should not contain key("next")
      props should not contain key("previous")
    }

    it("exposes annotated PagedSeq and Page.empty properties") {
      val paged = byName(MetaLoader.load(classOf[PagedSeq[String]]))
      paged("hasPrevious").getterName shouldBe "hasPrevious"
      paged("hasNext").getterName shouldBe "hasNext"

      val empty = byName(MetaLoader.load(Page.empty().getClass))
      empty("hasPrevious").getterName shouldBe "hasPrevious"
      empty("hasNext").getterName shouldBe "hasNext"
    }

    it("invokes annotated Page getters through BeanInfo") {
      val bi = BeanInfo.from(MetaLoader.load(classOf[SinglePage[String]]))
      val page = new SinglePage[String](2, 2, 5, List("c", "d"))
      bi.getGetterMethod("hasPrevious").get.invoke(page) shouldBe java.lang.Boolean.TRUE
      bi.getGetterMethod("hasNext").get.invoke(page) shouldBe java.lang.Boolean.TRUE
      bi.getGetterMethod("pageIndex").get.invoke(page) shouldBe Integer.valueOf(2)
    }

    it("reads annotated properties through Properties (template path)") {
      val first = new SinglePage[String](1, 2, 5, List("a", "b"))
      Properties.get[Boolean](first, "hasPrevious") shouldBe false
      Properties.get[Boolean](first, "hasNext") shouldBe true
    }

    it("supports implementations that rely on the annotated Page trait") {
      // 实现类自身不标注，靠 trait Page 上的注解注册（反射调用虚分派到实现）
      val props = byName(MetaLoader.load(classOf[BarePage]))
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("hasNext").getterName shouldBe "hasNext"
      Properties.get[Boolean](new BarePage, "hasPrevious") shouldBe false
    }
  }

  describe("MetaLoaderLite @property") {
    it("names annotated properties the same way as MetaLoader") {
      val props = byName(MetaLoaderLite.load(classOf[AnnotatedBean]))
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("title").getterName shouldBe "displayName"
      props("tag").setterName should contain("setTag")
      props should not contain key("displayName")
      props should not contain key("viaSetter")
    }

    it("stays lenient for unannotated parameterless defs") {
      byName(MetaLoaderLite.load(classOf[AnnotatedBean])) should contain key "plain"
    }

    it("ignores @property on zero-arg methods returning Unit") {
      val props = byName(MetaLoaderLite.load(classOf[AnnotatedBean]))
      props should not contain key("noop")
      props should not contain key("silent")
    }

    it("exposes annotated SinglePage properties") {
      val props = byName(MetaLoaderLite.load(classOf[SinglePage[String]]))
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("hasNext").getterName shouldBe "hasNext"
    }
  }

  describe("MetaDigger @property") {
    it("renames annotated properties at compile time") {
      val props = byName(MetaModels.of(classOf[AnnotatedBean]))
      props("title").getterName shouldBe "displayName"
      props should not contain key("displayName")
      props("hasPrevious").getterName shouldBe "hasPrevious"
      props("hasPrevious").typeinfo.clazz shouldBe classOf[Boolean]
    }

    it("ignores @property on methods with parameters") {
      val props = byName(MetaModels.of(classOf[AnnotatedBean]))
      props should not contain key("viaSetter")
      props("tag").setterName should contain("setTag")
    }

    it("ignores @property on zero-arg methods returning Unit") {
      val props = byName(MetaModels.of(classOf[AnnotatedBean]))
      props should not contain key("noop")
      props should not contain key("silent")
    }

    it("keeps dig and runtime reflection consistent") {
      val dug = pairs(MetaModels.of(classOf[AnnotatedBean]))
      val loaded = pairs(MetaLoader.load(classOf[AnnotatedBean]))
      Seq("hasPrevious", "hasNext", "title", "tag").foreach { name =>
        withClue(s"property $name: ") {
          dug.get(name) shouldBe loaded.get(name)
        }
      }
    }
  }
}

/** 测试用 bean：覆盖空 value、重命名、注解 setter（应被忽略）、与 @noreflect 组合。 */
class AnnotatedBean {
  private var tagValue: String = ""

  /** 无注解的参数less def：只有 MetaLoaderLite 会识别 */
  def plain: String = "plain"

  @property
  def hasPrevious: Boolean = false

  @property
  def hasNext: Boolean = true

  @property("title")
  def displayName: String = "beangle"

  @property("tag")
  def tag: String = tagValue

  /** 带参方法：注解忽略，仍按 setX 约定作为 tag 的 setter */
  @property("viaSetter")
  def setTag(value: String): Unit = { tagValue = value }

  /** 零参但返回 Unit：不是 getter，注解忽略 */
  @property("noop")
  def touch(): Unit = ()

  /** 无括号且返回 Unit：同样不是 getter，注解忽略 */
  @property("silent")
  def silent: Unit = ()

  @property("hidden")
  @noreflect
  def invisible: String = "hidden"
}

/** 只依赖 trait Page 注解的实现：自身方法未标注。 */
class BarePage extends Page[String] {
  override def totalPages: Int = 1

  override def pageIndex: Int = 1

  override def pageSize: Int = 10

  override def totalItems: Int = 2

  override def hasNext: Boolean = false

  override def hasPrevious: Boolean = false

  override def next(): Page[String] = this

  override def previous(): Page[String] = this

  override def moveTo(pageIndex: Int): Page[String] = this

  override def items: collection.Seq[String] = List("a", "b")

  override def apply(index: Int): String = items(index)

  override def length: Int = items.size

  override def iterator: Iterator[String] = items.iterator
}
