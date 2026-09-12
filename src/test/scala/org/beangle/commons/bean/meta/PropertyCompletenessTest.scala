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

import org.beangle.commons.collection.page.{Page, PagedSeq, SinglePage}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** 属性提取的黄金清单（API 稳定性护栏）。
  *
  * [[MetaLoader]]（JVM strict 反射）、[[MetaLoaderLite]]（native 轻量反射）、
  * [[MetaDigger]]（编译期 dig）三条路径提取出的属性必须「不多不少」，类型和读写器一致，
  * 避免将来优化（例如放行参数less 方法、切换 getMethods 策略）把 hashCode/getClass/
  * toString/copy 之类的无关方法带进 getter，或者漏掉已发布的属性。
  *
  * 约定：
  *  - 能三路对齐的类（Java bean、普通 Scala bean、注解属性）三路严格相等；
  *  - Page 实现继承自 scala.collection.Seq，lite/dig 天然更宽松（全部参数less 方法），
  *    因此只对 strict 严格相等，lite 校验「包含 + 同名同类同访问器 + 无垃圾属性」；
  *  - dig 目前无法处理 Seq 子类（`resolveType` 不支持 `ThisType`），故不列入 dig 校验。
  */
class PropertyCompletenessTest extends AnyFunSpec, Matchers {

  private case class Exp(name: String, tpe: Class[_], getter: String, setter: Option[String] = None)

  private def exp(items: Exp*): Map[String, Exp] = items.map(i => (i.name, i)).toMap

  private def e(name: String, tpe: Class[_], getter: String, setter: String = null): Exp =
    Exp(name, tpe, getter, Option(setter))

  /** 绝不能出现在属性里的方法（Object 方法、case class 样板、伴生方法）。 */
  private val junk = Set(
    "class", "hashCode", "toString", "wait", "notify", "notifyAll", "clone", "equals",
    "canEqual", "copy", "productArity", "productIterator", "productPrefix", "productElement",
    "productElementName", "productElementNames", "apply", "unapply", "unApply")

  private def check(label: String, cm: MetaModel.BeanMeta, expected: Map[String, Exp]): Unit = {
    val actual = cm.properties.map(p => (p.name, p)).toMap
    withClue(s"$label property names: ") {
      actual.keySet shouldBe expected.keySet
    }
    withClue(s"$label junk properties: ") {
      actual.keySet.intersect(junk) shouldBe empty
    }
    expected.foreach { case (name, ex) =>
      withClue(s"$label.$name: ") {
        val p = actual(name)
        p.typeinfo.clazz shouldBe ex.tpe
        p.getterName shouldBe ex.getter
        p.setterName shouldBe ex.setter
      }
    }
  }

  /** lite/dig 更宽松：要求覆盖 strict 清单，且同名属性的类型/访问器完全一致，并且没有垃圾属性。 */
  private def checkSuperset(label: String, cm: MetaModel.BeanMeta, expected: Map[String, Exp]): Unit = {
    val actual = cm.properties.map(p => (p.name, p)).toMap
    withClue(s"$label missing properties: ") {
      actual.keySet.intersect(expected.keySet) shouldBe expected.keySet
    }
    withClue(s"$label junk properties: ") {
      actual.keySet.intersect(junk) shouldBe empty
    }
    expected.foreach { case (name, ex) =>
      withClue(s"$label.$name: ") {
        val p = actual(name)
        p.typeinfo.clazz shouldBe ex.tpe
        p.getterName shouldBe ex.getter
        p.setterName shouldBe ex.setter
      }
    }
  }

  describe("PropertyCompleteness") {
    it("java bean: 只暴露 JavaBean 属性，不含 Object 方法") {
      val expected = exp(
        e("age", classOf[Int], "getAge", "setAge"),
        e("name", classOf[String], "getName", "setName"),
        e("writable", classOf[Boolean], "isWritable", "setWritable"))
      check("PlainJavaBean.strict", MetaLoader.load(classOf[PlainJavaBean]), expected)
      check("PlainJavaBean.lite", MetaLoaderLite.load(classOf[PlainJavaBean]), expected)
      check("PlainJavaBean.dig", MetaModels.of(classOf[PlainJavaBean]), expected)
    }

    it("write-only java bean: 只保留可读属性") {
      val expected = exp(e("name", classOf[String], "getName", "setName"))
      check("WriteOnlyBean.strict", MetaLoader.load(classOf[WriteOnlyBean]), expected)
      check("WriteOnlyBean.lite", MetaLoaderLite.load(classOf[WriteOnlyBean]), expected)
      check("WriteOnlyBean.dig", MetaModels.of(classOf[WriteOnlyBean]), expected)
    }

    it("scala bean + java 父类: 合并字段访问器与继承的 JavaBean 属性") {
      val expected = exp(
        e("id", classOf[Long], "id", "id_$eq"),
        e("title", classOf[String], "getTitle", "setTitle"))
      check("ScalaChildBean.strict", MetaLoader.load(classOf[ScalaChildBean]), expected)
      check("ScalaChildBean.lite", MetaLoaderLite.load(classOf[ScalaChildBean]), expected)
      check("ScalaChildBean.dig", MetaModels.of(classOf[ScalaChildBean]), expected)
    }

    it("只读 scala val: 只有 getter") {
      val expected = exp(e("code", classOf[String], "code"))
      check("LiteReadOnlyBean.strict", MetaLoader.load(classOf[LiteReadOnlyBean]), expected)
      check("LiteReadOnlyBean.lite", MetaLoaderLite.load(classOf[LiteReadOnlyBean]), expected)
      check("LiteReadOnlyBean.dig", MetaModels.of(classOf[LiteReadOnlyBean]), expected)
    }

    it("case class: 主构造参数成为属性，case 样板方法不进 getter") {
      val expected = exp(
        e("id", classOf[Long], "id"),
        e("name", classOf[String], "name"))
      check("LiteCtorBean.strict", MetaLoader.load(classOf[LiteCtorBean]), expected)
      check("LiteCtorBean.lite", MetaLoaderLite.load(classOf[LiteCtorBean]), expected)
      check("LiteCtorBean.dig", MetaModels.of(classOf[LiteCtorBean]), expected)
    }

    it("scala var + java 接口 getX/isX: 三路属性一致，仅 getterName 允许 lite 优先取 JavaBean 名") {
      val shared = exp(
        e("object", classOf[Object], "getObject"),
        e("singleton", classOf[Boolean], "isSingleton"),
        e("target", classOf[org.beangle.commons.bean.Factory[_]], "target", "target_$eq"))
      val expected = shared ++ exp(e("objectType", classOf[Class[_]], "objectType", "objectType_$eq"))
      check("FactoryBeanProxy.strict", MetaLoader.load(classOf[FactoryBeanProxy[?]]), expected)
      check("FactoryBeanProxy.dig", MetaModels.of(classOf[FactoryBeanProxy[?]]), expected)
      check(
        "FactoryBeanProxy.lite",
        MetaLoaderLite.load(classOf[FactoryBeanProxy[?]]),
        shared ++ exp(e("objectType", classOf[Class[_]], "getObjectType", "objectType_$eq")))
    }

    it("@property: 注解改名生效，带参/Unit/无注解方法的边界固定") {
      val common = exp(
        e("hasNext", classOf[Boolean], "hasNext"),
        e("hasPrevious", classOf[Boolean], "hasPrevious"),
        e("tag", classOf[String], "tag", "setTag"),
        e("title", classOf[String], "displayName"))
      val lenient = common ++ exp(e("plain", classOf[String], "plain"))
      check("AnnotatedBean.strict", MetaLoader.load(classOf[AnnotatedBean]), common)
      check("AnnotatedBean.lite", MetaLoaderLite.load(classOf[AnnotatedBean]), lenient)
      check("AnnotatedBean.dig", MetaModels.of(classOf[AnnotatedBean]), lenient)
    }

    it("SinglePage: 页面核心属性一个不少、一个不多") {
      val expected = exp(
        e("empty", classOf[Boolean], "isEmpty"),
        e("hasNext", classOf[Boolean], "hasNext"),
        e("hasPrevious", classOf[Boolean], "hasPrevious"),
        e("items", classOf[collection.Seq[_]], "items"),
        e("length", classOf[Int], "length"),
        e("pageIndex", classOf[Int], "pageIndex"),
        e("pageSize", classOf[Int], "pageSize"),
        e("totalItems", classOf[Int], "totalItems"),
        e("totalPages", classOf[Int], "totalPages"),
        e("traversableAgain", classOf[Boolean], "isTraversableAgain"))
      check("SinglePage.strict", MetaLoader.load(classOf[SinglePage[String]]), expected)
      checkSuperset("SinglePage.lite", MetaLoaderLite.load(classOf[SinglePage[String]]), expected)
    }

    it("PagedSeq: 页面核心属性一个不少、一个不多") {
      val expected = exp(
        e("datas", classOf[collection.immutable.Seq[_]], "datas"),
        e("empty", classOf[Boolean], "isEmpty"),
        e("hasNext", classOf[Boolean], "hasNext"),
        e("hasPrevious", classOf[Boolean], "hasPrevious"),
        e("page", classOf[Page[_]], "page", "page_$eq"),
        e("pageIndex", classOf[Int], "pageIndex", "pageIndex_$eq"),
        e("pageSize", classOf[Int], "pageSize"),
        e("totalPages", classOf[Int], "totalPages"),
        e("traversableAgain", classOf[Boolean], "isTraversableAgain"))
      check("PagedSeq.strict", MetaLoader.load(classOf[PagedSeq[String]]), expected)
      checkSuperset("PagedSeq.lite", MetaLoaderLite.load(classOf[PagedSeq[String]]), expected)
    }

    it("Page.empty: 只有注解过的页面属性可见") {
      val clazz = Page.empty().getClass
      val expected = exp(
        e("empty", classOf[Boolean], "isEmpty"),
        e("hasNext", classOf[Boolean], "hasNext"),
        e("hasPrevious", classOf[Boolean], "hasPrevious"),
        e("traversableAgain", classOf[Boolean], "isTraversableAgain"))
      check("EmptyPage.strict", MetaLoader.load(clazz), expected)
      checkSuperset("EmptyPage.lite", MetaLoaderLite.load(clazz), expected)
    }
  }
}
