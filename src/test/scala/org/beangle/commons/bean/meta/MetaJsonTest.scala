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

import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.commons.lang.reflect.{BeanInfo, BeanInfos}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import scala.collection.immutable.ArraySeq

/** MetaJson 调试导出测试（只出不入，无反向解析）。 */
class MetaJsonTest extends AnyFunSpec, Matchers {

  it("renders all sections of a ClassMeta as json") {
    val cm = BeanMetaConverter.from(BeanInfos.of(classOf[CodecSample]))
    val json = MetaJson.toJson(cm)
    json should startWith("{")
    json should endWith("}")
    json should include("\"clazz\":\"org.beangle.commons.bean.meta.CodecSample\"")
    json should include("\"name\":\"id\"")
    json should include("\"type\":\"Long\"")
    json should include("\"transient\":false")
    json should include("\"optional\":true") // age: Option[Int]
    json should include("\"name\":\"times\"")
    json should include("\"ctors\":[")
  }

  it("renders ctor parameter defaults as strings") {
    val cm = BeanMetaConverter.from(BeanInfos.of(classOf[CodecCtor]))
    val json = MetaJson.toJson(cm)
    json should include("\"default\":\"default\"") // name = "default"
    json should include("\"default\":\"true\"") // enabled = true
    json should include("\"default\":null") // id 无默认值
  }

  it("renders methods with params") {
    val getter = classOf[CodecRole].getDeclaredMethod("code")
    val bi = new BeanInfo(classOf[CodecRole], ArraySeq.empty, Map.empty, Map("code" -> ArraySeq(getter)))
    val json = MetaJson.toJson(BeanMetaConverter.from(bi))
    json should include("\"methods\":[{\"name\":\"code\"")
    json should include("\"params\":[]")
  }

  it("produces parseable json") {
    val json = MetaJson.toJson(BeanMetaConverter.from(BeanInfos.of(classOf[CodecSample])))
    Json.parse(json) shouldBe a[JsonObject]
  }

  it("binary form is much smaller than json form") {
    val cm = BeanMetaConverter.from(BeanInfos.of(classOf[CodecSample]))
    val binary = MetaCodec.encode(cm).length
    val json = MetaJson.toJson(cm).getBytes(StandardCharsets.UTF_8).length
    json should be > (binary * 2)
  }
}
