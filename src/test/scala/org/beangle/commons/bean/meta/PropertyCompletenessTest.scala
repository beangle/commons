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
  *  - Page 实现继承自 scala.collection.Seq，lite/dig 对工程内声明的参数less 方法更宽松，
  *    因此 strict 精确校验、lite 校验「包含 strict + 同名同类同访问器 + 无库方法」，
  *    dig 对标准库基类只收录 JavaBean 命名的成员（java.* 基类无成员树，不参与 dig）；
  *  - 任何提取器都不得把 scala.collection 的集合 API 变成 Bean 属性。
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

  /** scala.collection 一类标准库方法，绝不能作为 Bean 属性出现（含 Scala 3 mixin forwarder）。 */
  private val libraryApi = Set(
    "size", "knownSize", "hasDefiniteSize", "lengthIs", "sizeIs", "head", "headOption", "tail",
    "init", "last", "lastOption", "toList", "toSeq", "toSet", "toVector", "toStream", "toBuffer",
    "toIndexedSeq", "toIterable", "toIterator", "toTraversable", "mkString", "nonEmpty", "seq",
    "view", "permutations", "combinations", "distinct", "reverse", "reversed", "reverseIterator",
    "indices", "lift", "lifted", "zipWithIndex", "repr", "stringPrefix", "coll", "companion",
    "iterableFactory", "collectionClassName", "className", "newSpecificBuilder", "inits", "tails")

  private def checkClean(label: String, names: collection.Set[String]): Unit = {
    withClue(s"$label junk properties: ") { names.intersect(junk) shouldBe empty }
    withClue(s"$label library api: ") { names.intersect(libraryApi) shouldBe empty }
  }

  private def check(label: String, cm: MetaModel.BeanMeta, expected: Map[String, Exp]): Unit = {
    val actual = cm.properties.map(p => (p.name, p)).toMap
    withClue(s"$label property names: ") {
      actual.keySet shouldBe expected.keySet
    }
    checkClean(label, actual.keySet)
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
    checkClean(label, actual.keySet)
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

    it("继承 JDK bean 基类: lite 的 getter/setter 与 strict 一致（库收窄只作用于 getter）") {
      // java.util.Date 提供 getTime/setTime 等 JavaBean 访问器，用于验证库基类的 setter 不被丢弃
      class DateBean extends java.util.Date
      val strict = MetaLoader.load(classOf[DateBean]).properties.map(p => (p.name, p.getterName, p.setterName))
      val lite = MetaLoaderLite.load(classOf[DateBean]).properties.map(p => (p.name, p.getterName, p.setterName))
      lite shouldBe strict
      strict.map(_._1).contains("time") shouldBe true
    }

    it("工程内参数less 方法: strict 只认字段，lite/dig 全部收录（文档 4.1 示例）") {
      val strictProps = exp(
        e("items", classOf[collection.Seq[_]], "items"),
        e("pageIndex", classOf[Int], "pageIndex"))
      val lenientProps = strictProps ++ exp(
        e("hasNext", classOf[Boolean], "hasNext"),
        e("iterator", classOf[collection.Iterator[_]], "iterator"),
        e("size", classOf[Int], "size"),
        e("totalPages", classOf[Int], "totalPages"))
      check("PageBean.strict", MetaLoader.load(classOf[PageBean]), strictProps)
      // 工程内声明的参数less 方法（含 size）不受 libraryApi 黑名单约束，这里不复用 check
      val expectedNames = lenientProps.values.map(_.name).toSeq.sorted
      Seq(
        ("PageBean.lite", MetaLoaderLite.load(classOf[PageBean])),
        ("PageBean.dig", MetaModels.of(classOf[PageBean]))
      ).foreach { case (label, cm) =>
        withClue(s"$label property names: ") { cm.properties.map(_.name) shouldBe expectedNames }
        withClue(s"$label setters: ") { cm.properties.flatMap(_.setterName) shouldBe empty }
        cm.properties.find(_.name == "size").get.typeinfo.clazz shouldBe classOf[Int]
      }
    }

    it("Page 实现的 dig：JavaBean 风格属性与 strict 对齐，不含集合 API") {
      val pageProps = exp(
        e("hasNext", classOf[Boolean], "hasNext"),
        e("hasPrevious", classOf[Boolean], "hasPrevious"),
        e("items", classOf[collection.Seq[_]], "items"),
        e("iterator", classOf[collection.Iterator[_]], "iterator"),
        e("length", classOf[Int], "length"),
        e("pageIndex", classOf[Int], "pageIndex"),
        e("pageSize", classOf[Int], "pageSize"),
        e("totalItems", classOf[Int], "totalItems"),
        e("totalPages", classOf[Int], "totalPages"))
      // 标准库基类中 JavaBean 命名的成员（isEmpty/isTraversableAgain）照常收录，与 strict 一致
      val libraryProps = exp(
        e("empty", classOf[Boolean], "isEmpty"),
        e("traversableAgain", classOf[Boolean], "isTraversableAgain"))
      check("SinglePage.dig", MetaModels.of(classOf[SinglePage[String]]), pageProps ++ libraryProps)
      check("BarePage.dig", MetaModels.of(classOf[BarePage]), pageProps ++ libraryProps)
      check(
        "PagedSeq.dig",
        MetaModels.of(classOf[PagedSeq[String]]),
        pageProps ++ libraryProps ++ exp(
          e("datas", classOf[collection.immutable.Seq[_]], "datas"),
          e("page", classOf[Page[_]], "page", "page_$eq"),
          e("pageIndex", classOf[Int], "pageIndex", "pageIndex_$eq")))
      // dig 是 lite 的子集：dig 不含 lite 从工程内参数less 方法多认出来的成员
      val lite = MetaLoaderLite.load(classOf[SinglePage[String]]).properties.map(_.name).toSet
      (pageProps.keySet ++ libraryProps.keySet).subsetOf(lite) shouldBe true
    }
  }
}

/** 工程内参数less 方法示例，固定 strict/lite/dig 差异（见 docs/metamodel-loader-rules.md 4.1）。 */
class PageBean(val pageIndex: Int, val items: collection.Seq[String]) {
  def totalPages: Int = 1
  def hasNext: Boolean = false
  def iterator: Iterator[String] = items.iterator
  def size: Int = items.size
}
