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

import org.beangle.commons.aot.AotPolicy
import org.beangle.commons.bean.component
import org.beangle.commons.lang.reflect.TypeInfo.IterableType
import org.beangle.commons.lang.testbean.TestEnum
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class AppRegistry extends MetaRegistrar {

  override def registering(): Unit = {
    register(classOf[PersonMeta], classOf[OtherMeta])
    register(classOf[CodecMeta])
  }
}

@component
class EnumComponent {
  var level: TestEnum = _
}

class EnumEntity {
  var name: String = _
  var level: TestEnum = TestEnum.Public
  var levels: Seq[TestEnum] = Seq.empty
  var detail: EnumComponent = _
}

class EnumRegistry extends MetaRegistrar {
  override def registering(): Unit = register(classOf[EnumEntity])
}

class MetaRegistrarTest extends AnyFunSpec, Matchers {

  describe("MetaRegistrar") {
    it("registers classes through the registering template method") {
      val r = new AppRegistry
      r.registering()
      val metas = r.metas
      assert(metas.map(_.clazz).toSet == Set(classOf[PersonMeta], classOf[OtherMeta], classOf[CodecMeta]))
    }

    it("keeps compile-time type precision (Map[String,Int] keys)") {
      val r = new AppRegistry
      r.registering()
      val pmeta = r.metas.find(_.clazz == classOf[PersonMeta]).get
      val scores = pmeta.properties.find(_.name == "scores").get
      val args = scores.typeinfo.asInstanceOf[IterableType].args
      assert(args.map(_.clazz).toSet == Set(classOf[String], classOf[Int]))
    }

    it("registering is idempotent") {
      val r = new AppRegistry
      r.registering()
      assert(r.metas.size == 3)
      r.registering()
      assert(r.metas.size == 3)
    }

    it("encodes a beaninfo.idx into the given stream") {
      val r = new AppRegistry
      r.registering()
      val out = new ByteArrayOutputStream()
      r.encode(out)
      val metas = MetaIndex.read(new ByteArrayInputStream(out.toByteArray))
      // index directory is sorted by JVM class name
      assert(metas.map(_.clazz).toSet == Set(classOf[CodecMeta], classOf[OtherMeta], classOf[PersonMeta]))
    }

    it("registers enum property types recursively (components included)") {
      val r = new EnumRegistry
      r.registering()
      val types = r.aotHints.getTypePolicies.keySet
      types should contain allOf (classOf[EnumEntity], classOf[TestEnum], classOf[TestEnum.type])
      val enumPolicy = r.aotHints.getTypePolicies(classOf[TestEnum])
      enumPolicy.categories should contain(AotPolicy.Category.PublicFields)
    }
  }
}
