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

import org.beangle.commons.lang.Strings
import scala.collection.mutable.ListBuffer
/**
 * 排序
 *
 * @author chaostone
 * @version $Id: $
 */
object Order {
  /**
   * Constant <code>OrderStr="orderBy"</code>
   */
  val OrderStr = "orderBy"

  def apply(property:String) = new  Order(property, true)

  def apply(property:String,ascending: Boolean) = new  Order(property, ascending)


  /**
   * <p>
   * asc.
   * </p>
   *
   * @param property a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.collection.Order} object.
   */
  def asc(property: String): Order = new Order(property, true)

  /**
   * <p>
   * desc.
   * </p>
   *
   * @param property a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.collection.Order} object.
   */
  def desc(property: String): Order = new Order(property, false)

  /**
   * <p>
   * toSortString.
   * </p>
   *
   * @param orders a {@link java.util.List} object.
   * @return a {@link java.lang.String} object.
   */
  def toSortString(orders: List[Order]): String = {
    if (null == orders || orders.isEmpty) return ""
    val buf = new StringBuilder("order by ")
    for (order <- orders) {
      if (order.ascending) buf.append(order.property).append(',')
      else buf.append(order.property).append(" desc,")
    }
    buf.substring(0, buf.length - 1).toString
  }

  /**
   * <p>
   * parse.
   * </p>
   *
   * @param orderString a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  def parse(orderString: String): List[Order] = {
    if (Strings.isBlank(orderString)) {
      List()
    } else {
      val orders = new ListBuffer[Order]
      val orderStrs = Strings.split(orderString, ',')
      for (i <- 0 until orderStrs.length) {
        var order = orderStrs(i).trim()
        if (Strings.isNotBlank(order)) {
          order = order.toLowerCase().trim()
          if (order.endsWith(" desc")) {
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" desc")), false)
          } else if (order.endsWith(" asc")) {
            orders += new Order(orderStrs(i).substring(0, order.indexOf(" asc")), true)
          } else {
            orders += new Order(orderStrs(i), true)
          }
        }
      }
      orders.toList
    }
  }

  private def analysis(orderStr:String):(String,Boolean)={
    if (Strings.contains(orderStr, ",")) throw new RuntimeException("user parser for multiorder")

    var ascending=false
    var property=orderStr
    if (Strings.contains(property, " desc")) {
      ascending = false
      property = Strings.substringBefore(property, " desc")
    } else {
      property = if (Strings.contains(property, " asc")) Strings.substringBefore(property, " asc") else property
      ascending = true
    }
    property = property.trim()
    (property,ascending)
  }
}


/**
 * 排序
 *
 * @author chaostone
 * @version $Id: $
 */
class Order(val property: String,val ascending: Boolean,val lowerCase:Boolean =false) {

  /**
   * <p>
   * Constructor for Order.
   * </p>
   *
   * @param property a {@link java.lang.String} object.
   */
  def this(property: String) {
    this(Order.analysis(property)._1,Order.analysis(property)._2)
  }

  /**
   * <p>
   * toString.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  override def toString(): String = {
    if (lowerCase) "lower(" + property + ") " + (if (ascending) "asc" else "desc")
    else property + " " + (if (ascending) "asc" else "desc")
  }
}
