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

import org.beangle.commons.lang.Strings

import scala.collection.mutable.ListBuffer

/** 排序
  *
  * @author chaostone
  */
object Order {
  /** Constant <code>OrderStr="orderBy"</code>
    */
  val OrderStr = "orderBy"

  def apply(property: String) = new Order(property, true)

  def apply(property: String, ascending: Boolean) = new Order(property, ascending)

  /** Asc.
    *
    * @param property a String object.
    * @return a {@link org.beangle.commons.collection.Order} object.
    */
  def asc(property: String): Order = new Order(property, true)

  /** Desc.
    *
    * @param property a String object.
    * @return a {@link org.beangle.commons.collection.Order} object.
    */
  def desc(property: String): Order = new Order(property, false)

  /** toSortString.
    */
  def toSortString(orders: List[Order]): String = {
    if (null == orders || orders.isEmpty) return ""
    val buf = new StringBuilder("order by ")
    for (order <- orders)
      if (order.ascending) buf.append(order.property).append(',')
      else buf.append(order.property).append(" desc,")
    buf.substring(0, buf.length - 1).toString
  }

  /** parse order string.
    */
  def parse(orderString: String): List[Order] =
    if (Strings.isBlank(orderString)) {
      List.empty
    } else {
      val orders = new ListBuffer[Order]
      val orderStrs = Strings.split(orderString, ',')
      for (i <- 0 until orderStrs.length) {
        val originOrder = orderStrs(i)
        if (Strings.isNotBlank(originOrder)) {
          val order = originOrder.toLowerCase()
          if order.endsWith(" desc") then
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" desc")).trim(), false)
          else if order.endsWith(" asc") then
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" asc")).trim(), true)
          else if order.startsWith("-") then
            orders += new Order(orderStrs(i).trim().substring(1), false)
          else
            orders += new Order(orderStrs(i).trim(), true)
        }
      }
      orders.toList
    }

  private def analysis(orderStr: String): (String, Boolean) = {
    if (Strings.contains(orderStr, ",")) throw new RuntimeException("user parser for multiorder")

    var ascending = false
    var property = orderStr
    if (Strings.contains(property, " desc")) {
      ascending = false
      property = Strings.substringBefore(property, " desc")
    } else {
      property = if (Strings.contains(property, " asc")) Strings.substringBefore(property, " asc") else property
      ascending = true
    }
    property = property.trim()
    (property, ascending)
  }
}

/** 排序
  *
  * @author chaostone
  */
class Order(val property: String, val ascending: Boolean, val lowerCase: Boolean = false) {

  /** Constructor for Order.
    *
    * @param property a String object.
    */
  def this(property: String) = {
    this(Order.analysis(property)._1, Order.analysis(property)._2)
  }

  /** ToString.
    *
    * @return a String object.
    */
  override def toString: String =
    if (lowerCase) "lower(" + property + ") " + (if (ascending) "asc" else "desc")
    else property + " " + (if (ascending) "asc" else "desc")
}
