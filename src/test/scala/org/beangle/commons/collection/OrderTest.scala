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

package org.beangle.commons.collection

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class OrderTest extends AnyFunSpec, Matchers {

  describe("Order") {
    it("ToString1") {
      Order.toSortString(List(Order("teachPlan.grade desc"), Order("teachPlan.major.code"))) should equal("order by teachPlan.grade desc,teachPlan.major.code")
    }

    it("ToString2") {
      Order.toSortString(List(Order("id", false), Order.asc("name"))) should equal("order by id desc,name")
    }

    it("Parse minus order") {
      val orders = Order.parse("-std.code")
      orders.size should be(1)
      orders foreach { order =>
        order.ascending should be(false)
        order.property should equal("std.code")
      }
    }

    it("ParserOrder") {
      val orders = Order.parse("std.code asc")
      orders.size should be(1)
      orders foreach { order =>
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

      val a = Order.parse("semester.schoolYear desc, semester.name  desc")
      assert(a.size == 2)
      assert(a(1).property == "semester.name")
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
