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

import org.beangle.commons.lang.reflect.BeanInfo
import org.beangle.commons.bean.Factory
import org.beangle.commons.collection.page.SinglePage
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** MetaLoaderLite 契约：仅 public 构造器/方法，与 MetaLoader 在 JavaBean 类上保持一致，
 * 但不识别 Scala 字段访问器、不解析构造器默认值。 */
class MetaLoaderLiteTest extends AnyFunSpec, Matchers {

  describe("MetaLoaderLite") {
    it("loads java bean properties same as MetaLoader") {
      val cm = MetaLoaderLite.load(classOf[PlainJavaBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      byName.keySet shouldBe Set("name", "age", "writable")
      byName("name").getterName shouldBe "getName"
      byName("name").setterName should contain("setName")
      byName("age").getterName shouldBe "getAge"
      byName("age").setterName should contain("setAge")
      byName("writable").getterName shouldBe "isWritable"
      byName("writable").setterName should contain("setWritable")
    }

    it("keeps readable-only properties, excludes write-only setters") {
      val cm = MetaLoaderLite.load(classOf[WriteOnlyBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      byName.keySet shouldBe Set("name")
      byName("name").getterName shouldBe "getName"
      byName("name").setterName should contain("setName")
    }

    it("discovers scala var id and inherited java bean getters") {
      val cm = MetaLoaderLite.load(classOf[ScalaChildBean])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      byName.keySet shouldBe Set("id", "title")
      byName("id").getterName shouldBe "id"
      byName("id").setterName should contain("id_$eq")
      byName("title").getterName shouldBe "getTitle"
      byName("title").setterName should contain("setTitle")
    }

    it("discovers read-only scala val accessors as properties") {
      val lite = MetaLoaderLite.load(classOf[LiteReadOnlyBean])
      val byName = lite.properties.map(p => (p.name, p)).toMap
      byName.keySet shouldBe Set("code")
      byName("code").getterName shouldBe "code"
      byName("code").setterName shouldBe None
    }

    it("discovers read-only accessors on SinglePage (size-style and is-bridges)") {
      val cm = MetaLoaderLite.load(classOf[SinglePage[String]])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      byName.keySet should contain allOf(
        "pageIndex", "pageSize", "totalItems", "items", "totalPages",
        "hasNext", "hasPrevious", "size", "length", "empty", "traversableAgain")
      byName("pageIndex").getterName shouldBe "pageIndex"
      byName("pageIndex").setterName shouldBe None
      byName("items").getterName shouldBe "items"
      byName("size").getterName shouldBe "size"
      byName("empty").getterName shouldBe "isEmpty"
      byName("traversableAgain").getterName shouldBe "isTraversableAgain"
      // 主构造器参数形成的属性非 transient；size/is 型继承方法无 setter、非主构造器参数 → transient
      byName("pageIndex").isTransient shouldBe false
      byName("items").isTransient shouldBe false
      byName("size").isTransient shouldBe true
      byName("empty").isTransient shouldBe true
    }

    it("discovers public constructors without default values") {
      val cm = MetaLoaderLite.load(classOf[LiteCtorBean])
      cm.ctors should not be empty
      val primary = cm.ctors.head
      primary.parameters.map(_.typeinfo.clazz) shouldBe Seq(classOf[Long], classOf[String])
      primary.parameters.foreach(_.defaultValue shouldBe None)
    }

    it("discovers FactoryBeanProxy scala vars and java bean getters") {
      val cm = MetaLoaderLite.load(classOf[FactoryBeanProxy[_]])
      val byName = cm.properties.map(p => (p.name, p)).toMap
      byName.keySet shouldBe Set("object", "singleton", "objectType", "target")
      byName("object").getterName shouldBe "getObject"
      byName("object").setterName shouldBe None
      byName("singleton").getterName shouldBe "isSingleton"
      byName("singleton").setterName shouldBe None
      byName("objectType").getterName shouldBe "getObjectType"
      // Scala var 生成 objectType_$eq 赋值器（而非 setObjectType），作为 setter 挂到同名属性
      byName("objectType").setterName should contain("objectType_$eq")
      // Scala var target：setter target_$eq 触发同名参数less 方法 target() 作为 getter
      byName("target").getterName shouldBe "target"
      byName("target").setterName should contain("target_$eq")
    }

    it("reconstructs usable getter/setter handles on FactoryBeanProxy") {
      val bi = BeanInfo.from(MetaLoaderLite.load(classOf[FactoryBeanProxy[String]]))
      val proxy = new FactoryBeanProxy[String]
      bi.getSetter("objectType").get.invoke(proxy, classOf[String])
      bi.getGetter("objectType").get.invoke(proxy) shouldBe classOf[String]
      val factory = new Factory[String] {
        override def getObject: String = "prod"
      }
      bi.getSetter("target").get.invoke(proxy, factory)
      bi.getGetter("target").get.invoke(proxy) shouldBe factory
      bi.getGetter("object").get.invoke(proxy) shouldBe "prod"
    }

    it("supports BeanInfo.from accessor reconstruction") {
      val bi = BeanInfo.from(MetaLoaderLite.load(classOf[PlainJavaBean]))
      bi.getGetter("name") shouldBe defined
      bi.getSetter("name") shouldBe defined
      val entity = new PlainJavaBean
      entity.setName("lite")
      bi.getGetter("name").get.invoke(entity) shouldBe "lite"
    }

    it("MetaModels.reflect keeps full MetaLoader on standard JVM") {
      MetaModels.reflect(classOf[PlainJavaBean]).properties.map(_.name).toSet shouldBe Set("name", "age", "writable")
    }
  }
}

case class LiteCtorBean(id: Long, name: String)

/** 只读 Scala val：无 setter，验证参数less 方法也注册为只读 getter。 */
class LiteReadOnlyBean {
  val code: String = "x"
}

/** 对应 Spring FactoryBean 的接口形态（commons 无 Spring 依赖，测试内最小复刻）。 */
trait FactoryBean[T] {
  def getObject: T
  def isSingleton: Boolean
  def getObjectType: Class[T]
}

class FactoryBeanProxy[T] extends FactoryBean[T] {
  var target: Factory[T] = _
  var objectType: Class[T] = _

  override def getObject: T = target.getObject
  override def isSingleton: Boolean = target.singleton
  override def getObjectType: Class[T] = objectType
}
