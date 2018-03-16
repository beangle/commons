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
package org.beangle.commons.collection

import java.time.LocalDate

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MapConverterTest extends FunSpec with Matchers {

  val datas = Map[String, Any](("empty1", ""), ("empty2", null), ("empty3", List("")), ("empty4", Array(null)), ("number2", List("2")))

  var converter: MapConverter = new MapConverter()

  describe("MapConverter") {
    it("Convert Origin Type") {
      converter.convert("", classOf[String]) should be(Some(""))
    }

    it("Convert Date") {
      val year = 2010
      val month = 9
      val day = 1
      var dateDatas = this.datas + ("birthday" -> (year + "-" + month + "-" + day))
      val birthday = converter.getDate(dateDatas, "birthday").get
      birthday.getYear should be(year)
      birthday.getMonth.getValue should be(month)
      birthday.getDayOfMonth should be(day)
      converter.get(dateDatas, "birthday", classOf[LocalDate]) should equal(Some(birthday))
    }

    it("Get Null") {
      converter.getBoolean(datas, "empty1") should be(None)
      converter.getBoolean(datas, "empty2") should be(None)
      converter.getLong(datas, "empty1") should be(None)
      converter.getLong(datas, "empty2") should be(None)
      converter.getLong(datas, "empty3") should be(None)
      converter.getLong(datas, "number2") should be(Some(2))
      converter.getLong(datas, "empty4") should be(None)
      converter.getLong(datas, "emptyDoesNotExists") should be(None)
    }
    it("convert array") {
      converter.convert(Array("1", "2"), classOf[Long]) should equal(Array(1L, 2L))
    }
  }
}
