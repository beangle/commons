/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.collection

import java.sql.Date
import java.util.Calendar
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MapConverterTest extends FunSpec with Matchers {

  val datas = Map[String, Any](("empty1", ""), ("empty2", null), ("empty3", Array("")))

  var converter: MapConverter = new MapConverter()

  describe("MapConverter") {
    it("Convert Origin Type") {
      converter.convert("", classOf[String]) should be("")
    }

    it("Convert Date") {
      val year = 2010
      val month = 9
      val day = 1
      var dateDatas = this.datas + ("birthday" -> (year + "-" + month + "-" + day))
      val birthday = converter.get(dateDatas, "birthday", classOf[Date]).get
      val calendar = Calendar.getInstance
      calendar.setTime(birthday)
      calendar.get(Calendar.YEAR) should be(year)
      calendar.get(Calendar.MONTH) should be(month - 1)
      calendar.get(Calendar.DAY_OF_MONTH) should be(day)
      converter.get(dateDatas, "birthday", classOf[Date]) should equal(Some(birthday))
    }

    it("Get Null") {
      converter.getBoolean(datas, "empty1") should be(Some(false))
      converter.getBoolean(datas, "empty2") should be(None)
      converter.getLong(datas, "empty1") should be(Some(0))
      converter.getLong(datas, "empty2") should be(None)
      converter.getLong(datas, "empty3") should be(Some(0))
      converter.getLong(datas, "empty4") should be(None)
    }
  }
}
