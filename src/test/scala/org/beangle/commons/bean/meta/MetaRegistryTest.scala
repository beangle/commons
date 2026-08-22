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
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class AppRegistry extends MetaRegistry {
  override def registering(): Unit = {
    register(classOf[PersonMeta], classOf[OtherMeta])
    register(classOf[CodecMeta])
  }
}

class MetaRegistryTest extends AnyFunSpec, Matchers {

  describe("MetaRegistry") {
    it("collects registered classes through the registering template method") {
      val r = new AppRegistry
      val metas = r.collect()
      assert(metas.map(_.clazz) == Seq(classOf[PersonMeta], classOf[OtherMeta], classOf[CodecMeta]))
    }

    it("keeps compile-time type precision (Map[String,Int] keys)") {
      val r = new AppRegistry
      val scores = r.collect().head.properties.find(_.name == "scores").get
      val args = scores.typeinfo.asInstanceOf[IterableType].args
      assert(args.map(_.clazz) == Seq(classOf[String], classOf[Int]))
    }

    it("collect is idempotent (registering runs once)") {
      val r = new AppRegistry
      assert(r.collect().size == 3)
      assert(r.collect().size == 3)
    }

    it("encodes a beaninfo.idx into the given stream") {
      val r = new AppRegistry
      val out = new ByteArrayOutputStream()
      r.encode(out)
      val metas = MetaIndex.read(new ByteArrayInputStream(out.toByteArray))
      // index directory is sorted by JVM class name
      assert(metas.map(_.clazz) == Seq(classOf[CodecMeta], classOf[OtherMeta], classOf[PersonMeta]))
    }
  }
}
