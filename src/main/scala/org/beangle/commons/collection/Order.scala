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

/** Ordering specification for sorting (property with asc/desc direction).
 *
 * @author chaostone
 */
object Order {

  /** Default param name for order string (e.g. "orderBy=name,-age"). */
  val OrderStr = "orderBy"

  /** Creates an ascending Order for the given property. Use parse for comma-separated multiple orders.
   *
   * @param property the property name to sort by
   * @return ascending Order
   */
  def apply(property: String): Order = {
    require(!Strings.contains(property, ","), "use parse for multiple order")
    parse(property).head
  }

  /** Creates an ascending order for the property.
   *
   * @param property the property name to sort by
   * @return ascending Order
   */
  def asc(property: String): Order = {
    new Order(property, true)
  }

  /** Creates a descending order for the property.
   *
   * @param property the property name to sort by
   * @return descending Order
   */
  def desc(property: String): Order = new Order(property, false)

  /** Converts the order list to SQL "order by" clause string.
   *
   * @param orders the list of Order specifications
   * @return the order by string (e.g. "order by name, age desc")
   */
  def toSortString(orders: List[Order]): String = {
    if (null == orders || orders.isEmpty) return ""
    val buf = new StringBuilder("order by ")
    for (order <- orders)
      if (order.ascending) buf.append(order.property).append(',')
      else buf.append(order.property).append(" desc,")
    buf.substring(0, buf.length - 1)
  }

  /** Parses an order string (e.g. "name,-age,+score") into a list of Order.
   * Supports: "name desc", "name asc", "-name" (desc), "+name" (asc).
   *
   * @param orderString the comma-separated order string
   * @return the list of Order
   */
  def parse(orderString: String): List[Order] =
    if (Strings.isBlank(orderString)) {
      List.empty
    } else {
      val orders = new ListBuffer[Order]
      val orderStrs = Strings.split(orderString, ',')
      for (i <- orderStrs.indices) {
        val originOrder = orderStrs(i)
        if (Strings.isNotBlank(originOrder)) {
          val order = originOrder.toLowerCase()
          if order.endsWith(" desc") then
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" desc")).trim(), false)
          else if order.endsWith(" asc") then
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" asc")).trim(), true)
          else if order.startsWith("-") then
            orders += new Order(orderStrs(i).trim().substring(1), false)
          else if order.startsWith("+") then
            orders += new Order(orderStrs(i).trim().substring(1), true)
          else
            orders += new Order(orderStrs(i).trim(), true)
        }
      }
      orders.toList
    }
}

/** Ordering specification: property name with ascending/descending direction.
 *
 * @param property  the property name to sort by
 * @param ascending true for asc, false for desc
 * @author chaostone
 */
case class Order(property: String, ascending: Boolean) {

  /** Returns string representation (e.g. "name asc" or "age desc"). */
  override def toString: String = {
    property + " " + (if (ascending) "asc" else "desc")
  }

}
