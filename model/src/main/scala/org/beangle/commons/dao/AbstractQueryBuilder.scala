/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.data.dao

import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.dao._
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings._

object AbstractQueryBuilder {
  val InnerJoin = " left join "
  val OuterJoin = " outer join "
  val RightOuterJoin = " right outer join "
}
/**
 * Abstract AbstractQueryBuilder class.
 *
 * @author chaostone
 */
abstract class AbstractQueryBuilder[T] extends QueryBuilder[T] {

  protected var statement: String = _

  protected var limit: PageLimit = _

  val params = new collection.mutable.HashMap[String, Any]

  protected var select: String = _

  protected var from: String = _

  var alias: String = _

  protected var conditions: List[Condition] = Nil

  protected var orders: List[Order] = Nil

  protected var groups: List[String] = Nil

  protected var having: String = _

  protected var cacheable = false

  def build(): Query[T] = {
    val queryBean = new QueryBean[T]()
    queryBean.statement = genStatement()
    queryBean.params = params.toMap
    queryBean.limit = limit
    queryBean.countStatement = genCountStatement()
    queryBean.cacheable = cacheable
    queryBean.lang = lang
    queryBean
  }

  def lang: Query.Lang

  def select(what: String): this.type = {
    this.select = if (null == what) {
      null
    } else {
      if (what.toLowerCase.trim().startsWith("select")) what else "select " + what
    }
    this
  }

  def newFrom(from: String): this.type = {
    this.from = if (null == from) {
      null
    } else {
      if (contains(from.toLowerCase(), "from")) from else "from " + from
    }
    this
  }

  def alias(alias: String): this.type = {
    this.alias = alias
    this
  }

  def limit(limit: PageLimit): this.type = {
    this.limit = limit
    this
  }

  def limit(pageIndex: Int, pageSize: Int): this.type = {
    this.limit = PageLimit(pageIndex, pageSize)
    this
  }

  def cacheable(cacheable: Boolean = true): this.type = {
    this.cacheable = true
    this
  }

  def join(path: String, alias: String): this.type = {
    from = concat(from, " join ", path, " ", alias)
    this
  }

  def join(joinMode: String, path: String, alias: String): this.type = {
    from = concat(from, " ", joinMode, " join ", path, " ", alias)
    this
  }

  def params(newparams: collection.Map[String, Any]): this.type = {
    this.params ++= newparams
    this
  }

  def param(name: String, value: Any): this.type = {
    params += (name -> value)
    this
  }

  def where(condition: Condition): this.type = {
    if (isNotEmpty(statement))
      throw new RuntimeException("cannot add condition to a exists statement")
    conditions = conditions :+ condition
    params(Conditions.getParamMap(condition))
  }

  def where(cons: Seq[Condition]): this.type = {
    conditions = conditions ++ cons
    params(Conditions.getParamMap(cons))
  }

  def where(content: String, params: Any*): this.type = where(new Condition(content, params: _*))

  def orderBy(order: String): this.type = {
    orderBy(Order.parse(order))
    this
  }

  def orderBy(index: Int, order: String): this.type = {
    if (isNotEmpty(statement)) throw new RuntimeException("cannot add order by to a exists statement.")
    this.orders = this.orders.slice(0, index) ::: Order.parse(order) ::: this.orders.slice(index, this.orders.size)
    this
  }

  def orderBy(order: Order): this.type = {
    if (null != order) orderBy(List(order))
    this
  }

  def clearOrders(): this.type = {
    this.orders = Nil
    this
  }

  def clearGroups(): this.type = {
    this.groups = Nil
    this
  }

  def orderBy(orders: List[Order]): this.type = {
    if (null != orders) {
      if (isNotEmpty(statement)) throw new RuntimeException("cannot add order by to a exists statement.")
      this.orders = this.orders ::: orders
    }
    this
  }

  def groupBy(what: String): this.type = {
    if (isNotEmpty(what)) groups = groups :+ what
    this
  }

  def having(what: String): this.type = {
    Assert.isTrue(!groups.isEmpty)
    if (isNotEmpty(what)) having = what
    this
  }

  protected def genStatement(): String = {
    if (isNotEmpty(statement)) statement
    else genQueryStatement(true)
  }

  protected def genCountStatement(): String

  protected def genQueryStatement(hasOrder: Boolean): String = {
    if (null == from) statement
    val buf = new StringBuilder(50)
    if (null != select) buf.append(select + " ").append(from)
    if (!conditions.isEmpty) buf.append(" where ").append(Conditions.toQueryString(conditions))

    if (!groups.isEmpty) {
      buf.append(" group by ")
      for (groupBy <- groups) buf.append(groupBy).append(',')
      buf.deleteCharAt(buf.length() - 1)
    }
    if (null != having) buf.append(" having ").append(having)
    if (hasOrder && !orders.isEmpty) buf.append(' ').append(Order.toSortString(orders))
    buf.mkString
  }

}
