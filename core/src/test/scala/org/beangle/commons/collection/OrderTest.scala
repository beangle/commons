/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.collection

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class OrderTest extends FunSpec with ShouldMatchers {

  describe("Order") {
    it("ToString1") {
      Order.toSortString(List(Order("teachPlan.grade desc"), Order("teachPlan.major.code"))) should equal("order by teachPlan.grade desc,teachPlan.major.code")
    }

    it("ToString2") {
      Order.toSortString(List(Order("id", false), Order.asc("name"))) should equal("order by id desc,name")
    }

    it("ParserOrder") {
      val orders = Order.parse("std.code asc")
      orders.size should be(1)
      for (order <- orders) {
        order.ascending should be(true)
        order.property should equal("std.code")
      }
    }

    it("ParserMutiOrder") {
      val sorts = Order.parse("activity.time.year desc,activity.time.validWeeksNum,activity.time.weekId desc")
      sorts.size should be(3)
      var order = sorts(0).asInstanceOf[Order]
      order.property should equal("activity.time.year")
      order.ascending should be(false)
      order = sorts(1).asInstanceOf[Order]
      order.property should equal("activity.time.validWeeksNum")
      order.ascending should be(true)
      order = sorts(2).asInstanceOf[Order]
      order.property should equal("activity.time.weekId")
      order.ascending should be(false)
    }

    it("ParserComplexOrder") {
      val sorts = Order.parse("(case when ware.price is null then 0 else ware.price end) desc")
      sorts.size should be(1)
      val order = sorts(0).asInstanceOf[Order]
      order.property should equal("(case when ware.price is null then 0 else ware.price end)")
      order.ascending should be(false)
    }
  }
}
