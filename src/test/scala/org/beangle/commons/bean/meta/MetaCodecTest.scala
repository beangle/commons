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

import org.beangle.commons.lang.reflect.TypeInfo.IterableType
import org.beangle.commons.lang.reflect.{BeanInfo, BeanInfos, TypeInfo}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.lang.invoke.MethodHandles
import java.nio.file.Files
import java.sql.Date
import scala.collection.immutable.ArraySeq
import scala.collection.mutable

class CodecBase {
  var base: Int = 0
}

class CodecRole(var code: String)

class CodecSample(var id: Long, var name: String = "default") extends CodecBase {
  var age: Option[Int] = None
  var roles: java.util.Set[CodecRole] = new java.util.HashSet[CodecRole]()
  var times: mutable.Map[Int, Date] = mutable.Map.empty
  var enabled: Boolean = true
}

case class CodecCtor(id: Long, name: String = "default", enabled: Boolean = true)

class CodecTime(var startOn: java.time.LocalDate, var created: java.util.Date)

class CodecCollections(
  var roles: scala.collection.mutable.Set[String],
  var tags: scala.collection.immutable.List[String],
  var attrs: scala.collection.immutable.Map[String, Int],
  var buffer: scala.collection.mutable.ArrayBuffer[String])

class CodecJson(
  var attrs: org.beangle.commons.json.JsonObject,
  var list: org.beangle.commons.json.JsonArray,
  var raw: org.beangle.commons.json.Json,
  var value: org.beangle.commons.json.JsonValue,
  var props: java.util.Properties)

class CodecValue(
  var weekTime: org.beangle.commons.lang.time.WeekTime,
  var hourMinute: org.beangle.commons.lang.time.HourMinute,
  var amount: org.beangle.commons.lang.math.Decimal5,
  var tiny: org.beangle.commons.lang.math.TinyDecimal5,
  var uuid: java.util.UUID,
  var total: java.math.BigDecimal,
  var num: java.lang.Number = null,
  var locale: java.util.Locale = null)

/** MetaCodec v2 binary round-trip tests（parse 产物为纯数据，不含 Method 解析）。 */
class MetaCodecTest extends AnyFunSpec, Matchers {

  it("round-trip parse preserves properties and type precision from compile-time dig") {
    val bi = BeanInfos.of(classOf[CodecSample])
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(bi)))

    parsed.clazz shouldBe classOf[CodecSample]
    parsed.properties.map(_.name) should contain allOf ("id", "name", "age", "roles", "times", "base", "enabled")

    // 基本类型
    parsed.properties.find(_.name == "id").get.typeinfo.clazz shouldBe java.lang.Long.TYPE
    parsed.properties.find(_.name == "name").get.typeinfo.clazz shouldBe classOf[String]
    parsed.properties.find(_.name == "enabled").get.typeinfo.clazz shouldBe java.lang.Boolean.TYPE
    // option：属性存元素类型 + isOptional 标志（压平后无 option 包装）
    val age = parsed.properties.find(_.name == "age").get
    age.isOptional shouldBe true
    age.typeinfo.clazz shouldBe java.lang.Integer.TYPE
    parsed.properties.find(_.name == "id").get.isOptional shouldBe false
    // precision: Map[Int, Date] key stays int, not Object
    val times = parsed.properties.find(_.name == "times").get.typeinfo.asInstanceOf[IterableType]
    times.clazz shouldBe classOf[mutable.Map[_, _]]
    times.args(0).clazz shouldBe java.lang.Integer.TYPE
    times.args(1).clazz shouldBe classOf[Date]
    // collection of entity
    parsed.properties.find(_.name == "roles").get.typeinfo.asInstanceOf[IterableType].args(0).clazz shouldBe classOf[CodecRole]
    // 纯声明记录：无方法信息（访问器由构造期命名约定发现）
    val id = parsed.properties.find(_.name == "id").get
    id.isTransient shouldBe false
    id.isOptional shouldBe false
    parsed.properties.find(_.name == "base").get.isTransient shouldBe false
    // 编码尺寸（JSON v1 同规模约 7KB）
    MetaCodec.encode(BeanMetaConverter.from(bi)).length should be < 2048
  }

  it("round-trips constructor parameters with default values") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecCtor]))))
    parsed.ctors should not be empty
    val primary = parsed.ctors.head
    primary.parameters.map(_.name) shouldBe Seq("id", "name", "enabled")
    primary.parameters(1).defaultValue shouldBe Some("default")
    primary.parameters(2).defaultValue shouldBe Some(true)
  }

  it("round-trips transient flag and manual BeanInfo") {
    val clazz = classOf[CodecRole]
    val ti = TypeInfo.get(classOf[String])
    val getter = MethodHandles.lookup().unreflect(clazz.getDeclaredMethod("code"))
    val setter = MethodHandles.lookup().unreflect(clazz.getDeclaredMethod("code_$eq", classOf[String]))
    val p = new BeanInfo.PropertyInfo("code", ti, Some(getter), Some(setter), isTransient = true)
    val bi = new BeanInfo(clazz, ArraySeq.empty, Map("code" -> p), Map.empty)
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(bi)))
    val code = parsed.properties.find(_.name == "code").get
    code.isTransient shouldBe true
  }

  it("serializes methods; parse keeps them as raw records without resolving") {
    val getter = classOf[CodecRole].getDeclaredMethod("code")
    val withMethods = new BeanInfo(classOf[CodecRole], ArraySeq.empty, Map.empty, Map("code" -> ArraySeq(getter)))
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(withMethods)))
    parsed.methods shouldBe Seq(MetaModel.Method("code", Seq.empty))
  }

  it("property handles read and write bean properties") {
    val bi = BeanInfos.of(classOf[CodecSample])
    val sample = new CodecSample(5L, "n")
    val idGetter = bi.getGetter("id").get
    val nameGetter = bi.getGetter("name").get
    val ageGetter = bi.getGetter("age").get
    val idSetter = bi.getSetter("id").get
    val nameSetter = bi.getSetter("name").get
    val ageSetter = bi.getSetter("age").get
    // getter handle（基本类型经 invoke 自动装箱）
    val id0 = idGetter.invoke(sample)
    id0.asInstanceOf[Long] shouldBe 5L
    val name0 = nameGetter.invoke(sample)
    name0 shouldBe "n"
    // setter handle
    idSetter.invoke(sample, 7L)
    sample.id shouldBe 7L
    nameSetter.invoke(sample, "x")
    sample.name shouldBe "x"
    // option 属性
    ageSetter.invoke(sample, Some(3))
    sample.age shouldBe Some(3)
    val age0 = ageGetter.invoke(sample)
    age0 shouldBe Some(3)
    bi.getGetter("id") shouldBe defined
    bi.getSetter("id") shouldBe defined
  }

  it("exports parsed meta model as debug json (no reverse parse)") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecSample]))))
    val json = MetaJson.toJson(parsed)
    json should startWith("{")
    json should endWith("}")
    json should include("\"clazz\":\"org.beangle.commons.bean.meta.CodecSample\"")
    json should include("\"name\":\"id\"")
    json should include("\"transient\":false")
    json should include("\"optional\":true") // age: Option[Int]
    json should include("\"name\":\"times\"")
  }

  it("round-trips java.time and date types via builtin indices") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecTime]))))
    parsed.properties.find(_.name == "startOn").get.typeinfo.clazz shouldBe classOf[java.time.LocalDate]
    parsed.properties.find(_.name == "created").get.typeinfo.clazz shouldBe classOf[java.util.Date]
    // 内置索引不占池：显式池只有类名 + 两个属性名
    val bytes = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecTime])))
    ((bytes(10) & 0xff) << 8 | (bytes(11) & 0xff)) shouldBe 3
  }

  it("round-trips scala collection types via builtin indices") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecCollections]))))
    parsed.properties.find(_.name == "roles").get.typeinfo.clazz shouldBe classOf[scala.collection.mutable.Set[_]]
    parsed.properties.find(_.name == "tags").get.typeinfo.clazz shouldBe classOf[scala.collection.immutable.List[_]]
    parsed.properties.find(_.name == "attrs").get.typeinfo.clazz shouldBe classOf[scala.collection.immutable.Map[_, _]]
    parsed.properties.find(_.name == "buffer").get.typeinfo.clazz shouldBe classOf[scala.collection.mutable.ArrayBuffer[_]]
    // 集合类型全部走内置索引：显式池 = 类名 + 4 个属性名（元素 String/Int 也是内置）
    val bytes = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecCollections])))
    ((bytes(10) & 0xff) << 8 | (bytes(11) & 0xff)) shouldBe 5
  }

  it("round-trips beangle value types and common data-model types via builtin indices") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecValue]))))
    parsed.properties.find(_.name == "weekTime").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.lang.time.WeekTime]
    parsed.properties.find(_.name == "hourMinute").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.lang.time.HourMinute]
    parsed.properties.find(_.name == "amount").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.lang.math.Decimal5]
    parsed.properties.find(_.name == "tiny").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.lang.math.TinyDecimal5]
    parsed.properties.find(_.name == "uuid").get.typeinfo.clazz shouldBe classOf[java.util.UUID]
    parsed.properties.find(_.name == "total").get.typeinfo.clazz shouldBe classOf[java.math.BigDecimal]
    parsed.properties.find(_.name == "num").get.typeinfo.clazz shouldBe classOf[java.lang.Number]
    parsed.properties.find(_.name == "locale").get.typeinfo.clazz shouldBe classOf[java.util.Locale]
    // 全部走内置索引：显式池 = 类名 + 8 个属性名
    val bytes = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecValue])))
    ((bytes(10) & 0xff) << 8 | (bytes(11) & 0xff)) shouldBe 9
  }

  it("round-trips Properties and commons Json value types via builtin indices") {
    val parsed = MetaCodec.parse(MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecJson]))))
    parsed.properties.find(_.name == "attrs").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.json.JsonObject]
    parsed.properties.find(_.name == "list").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.json.JsonArray]
    parsed.properties.find(_.name == "raw").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.json.Json]
    parsed.properties.find(_.name == "value").get.typeinfo.clazz shouldBe classOf[org.beangle.commons.json.JsonValue]
    parsed.properties.find(_.name == "props").get.typeinfo.clazz shouldBe classOf[java.util.Properties]
    // 全部走内置索引：显式池 = 类名 + 5 个属性名
    val bytes = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecJson])))
    ((bytes(10) & 0xff) << 8 | (bytes(11) & 0xff)) shouldBe 6
  }

  it("writes and reads beaninfo index with directory lookup") {
    val metas = Seq(
      BeanMetaConverter.from(BeanInfos.of(classOf[CodecSample])),
      BeanMetaConverter.from(BeanInfos.of(classOf[CodecRole])),
      BeanMetaConverter.from(BeanInfos.of(classOf[CodecCtor])))
    val file = Files.createTempFile("beaninfo", ".idx")
    try {
      MetaIndex.write(file, metas)
      // 目录序 == blob 序，read 全量
      val all = MetaIndex.read(file)
      all.map(_.clazz) should contain allOf (classOf[CodecSample], classOf[CodecRole], classOf[CodecCtor])
      // 按 JVM 内部名定位单个类
      val found = MetaIndex.find(file, classOf[CodecSample].getName.replace('.', '/')).get
      found.clazz shouldBe classOf[CodecSample]
      found.properties.map(_.name) should contain allOf ("id", "name", "age", "times", "base")
      found.properties.find(_.name == "age").get.isOptional shouldBe true
      // 不存在的类
      MetaIndex.find(file, "no/such/Class") shouldBe empty
    } finally Files.deleteIfExists(file)
  }

  it("rejects bad magic and version") {
    intercept[IllegalArgumentException] { MetaCodec.parse(Array[Byte](1, 2, 3, 4)) }
    val badVersion = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecRole])))
    badVersion(4) = 99.toByte
    intercept[IllegalArgumentException] { MetaCodec.parse(badVersion) }
  }

  it("skips unknown sections") {
    val bytes = MetaCodec.encode(BeanMetaConverter.from(BeanInfos.of(classOf[CodecRole])))
    val extra = bytes ++ Array[Byte](99.toByte, 0, 0, 0, 0) // unknown tag 99, length 0
    MetaCodec.parse(extra).clazz shouldBe classOf[CodecRole]
  }
}
