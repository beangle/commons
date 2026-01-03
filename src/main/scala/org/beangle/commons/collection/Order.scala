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

  def apply(property: String): Order = {
    require(!Strings.contains(property, ","), "use parse for multiple order")
    parse(property).head
  }

  /** Asc.
   *
   * @param property a String object.
   * @return a order object.
   */
  def asc(property: String): Order = {
    new Order(property, true)
  }

  /** Desc.
   *
   * @param property a String object.
   * @return a order object.
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
    buf.substring(0, buf.length - 1)
  }

  /** parse order string.
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

/** 排序
 *
 * @author chaostone
 */
case class Order(property: String, ascending: Boolean) {

  /** ToString.
   *
   * @return a String object.
   */
  override def toString: String = {
    property + " " + (if (ascending) "asc" else "desc")
  }

}
