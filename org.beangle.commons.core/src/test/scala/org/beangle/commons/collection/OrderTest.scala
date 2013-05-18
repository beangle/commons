/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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


import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class OrderTest {

  @Test
  def testToString1() {
    assertEquals(Order.toSortString(List(new Order(" teachPlan.grade desc "),new Order(" teachPlan.major.code "))), "order by teachPlan.grade desc,teachPlan.major.code")
  }

  @Test
  def testToString() {
    assertEquals(Order.toSortString(List(new Order("id", false),Order.asc("name"))), "order by id desc,name")
  }

  def testParserOrder() {
    val orders = Order.parse("std.code asc")
    for (order <- orders) {
      assertTrue(order.isAscending)
      assertEquals(order.getProperty, "std.code")
    }
  }

  @Test
  def testParserMutiOrder() {
    val sorts = Order.parse("activity.time.year desc,activity.time.validWeeksNum,activity.time.weekId desc")
    assertEquals(sorts.size, 3)
    var order = sorts(0).asInstanceOf[Order]
    assertEquals(order.getProperty, "activity.time.year")
    assertFalse(order.isAscending)
    order = sorts(1).asInstanceOf[Order]
    assertEquals(order.getProperty, "activity.time.validWeeksNum")
    assertTrue(order.isAscending)
    order = sorts(2).asInstanceOf[Order]
    assertEquals(order.getProperty, "activity.time.weekId")
    assertFalse(order.isAscending)
  }

  @Test
  def testParserComplexOrder() {
    val sorts = Order.parse("(case when ware.price is null then 0 else ware.price end) desc")
    assertEquals(sorts.size, 1)
    val order = sorts(0).asInstanceOf[Order]
    assertEquals(order.getProperty, "(case when ware.price is null then 0 else ware.price end)")
    assertFalse(order.isAscending)
  }
}
