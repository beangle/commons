/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.conversion.converter

import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.util

@RunWith(classOf[JUnitRunner])
class IterableConverterTest extends AnyFunSpec with Matchers {

  describe("Iterable Converter") {
    it("Convert java iterable to scala") {
      val c = IterableConverterFactory
      val seq = c.convert(new util.ArrayList[Integer], classOf[collection.Seq[Integer]])
      seq.isInstanceOf[collection.Seq[Integer]] should be(true)

      val iseq = c.convert(new util.ArrayList[Integer], classOf[collection.immutable.Seq[Integer]])
      iseq.isInstanceOf[collection.immutable.Seq[Integer]] should be(true)
      val mc = MapConverterFactory
      val map = mc.convert(new util.HashMap[String, String], classOf[collection.mutable.Map[String, String]])
      map shouldNot equal(null)
    }
  }
}
