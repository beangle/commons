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
package org.beangle.commons.jpa.dao

import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.entity.dao._
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings._

object AbstractQueryBuilder {
  val InnerJoin = " left join "
  val OuterJoin = " outer join "
  val RightOuterJoin = " right outer join "
}
/**
 * <p>
 * Abstract AbstractQueryBuilder class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
abstract class AbstractQueryBuilder[T] extends QueryBuilder[T] {

  /** query 查询语句 */
  protected var statement: String = _

  /** 分页 */
  protected var limit: PageLimit = _

  /** 参数 */
  protected var paramMap: Map[String, Any] = _

  protected var select: String = _

  protected var from: String = _

  /** 别名 */
  var alias: String = _

  protected var conditions: List[Condition] = Nil

  protected var orders: List[Order] = Nil

  protected var groups: List[String] = Nil

  protected var having: String = _

  /** 缓存查询结果 */
  protected var cacheable = false

  /**
   * Returns params.
   */
  def params: Map[String, Any] = if (null == paramMap) Conditions.getParamMap(conditions) else paramMap

  /**
   * <p>
   * build.
   * </p>
   *
   * @return a {@link org.beangle.commons.dao.query.Query} object.
   */
  def build(): Query[T] = {
    val queryBean = new QueryBean[T]();
    queryBean.statement = genStatement()
    queryBean.params = params
    queryBean.limit = limit
    queryBean.countStatement = genCountStatement()
    queryBean.cacheable = cacheable
    queryBean.lang = lang
    queryBean
  }

  def lang: Query.Lang
  /**
   * <p>
   * select.
   * </p>
   *
   * @param what a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.dao.query.builder.OqlBuilder} object.
   */
  def select(what: String): this.type = {
    if (null == what) {
      this.select = null
    } else {
      if (what.toLowerCase.trim().startsWith("select")) {
        this.select = what
      } else {
        this.select = "select " + what
      }
    }
    this
  }
  /**
   * newFrom.
   *
   */
  def newFrom(from: String): this.type = {
    if (null == from) {
      this.from = null
    } else {
      if (contains(from.toLowerCase(), "from")) {
        this.from = from
      } else {
        this.from = "from " + from
      }
    }
    this
  }

  /**
   * alias.
   */
  def alias(alias: String): this.type = {
    this.alias = alias;
    this
  }

  def limit(limit: PageLimit): this.type = {
    this.limit = limit
    this
  }

  def limit(pageNo: Int, pageSize: Int): this.type = {
    this.limit = PageLimit(pageNo, pageSize)
    this
  }

  def cache(): this.type = {
    this.cacheable = true
    this
  }

  def cache(cacheable: Boolean): this.type = {
    this.cacheable = cacheable
    this
  }
  /**
   * join
   *
   * @param path a {@link java.lang.String} object.
   * @param alias a {@link java.lang.String} object.
   * @return a {@link org.beangle.commons.jpa.dao.OqlBuilder} object.
   */
  def join(path: String, alias: String): this.type = {
    from = concat(from, " join ", path, " ", alias)
    this
  }

  /**
   * join
   *
   * @param joinMode a {@link java.lang.String} object.
   * @param path a {@link java.lang.String} object.
   * @param alias a {@link java.lang.String} object.
   *
   */
  def join(joinMode: String, path: String, alias: String): this.type = {
    from = concat(from, " ", joinMode, " join ", path, " ", alias)
    this
  }

  def params(newparams: collection.Map[String, Any]): this.type = {
    this.paramMap = newparams.toMap
    this
  }

  /**
   * param.
   *
   * @param name a {@link java.lang.String} object.
   * @param value a {@link java.lang.Object} object.
   *
   */
  def param(name: String, value: Any): this.type = {
    paramMap = paramMap + (name -> value)
    this
  }

  /**
   * where
   *
   * @param condition a {@link org.beangle.commons.dao.query.builder.Condition} object.
   */
  def where(condition: Condition): this.type = {
    if (isNotEmpty(statement)) {
      throw new RuntimeException(
        "cannot add condition to a exists statement")
    }
    conditions = conditions :+ condition
    this
  }

  /**
   * 添加一组条件[br]
   * query中不能添加条件集合作为一个条件,因此这里命名没有采用有区别性的addAll
   *
   * @param cons a {@link java.util.Collection} object.
   * @return a {@link org.beangle.commons.jpa.dao.OqlBuilder} object.
   */
  def where(cons: Seq[Condition]): this.type = {
    conditions = conditions ++ cons
    this
  }

  /**
   * where.
   *
   * @param content a {@link java.lang.String} object.
   */
  def where(content: String, params: Any*): this.type = where(new Condition(content, params: _*))
  /**
   * 声明排序字符串
   *
   * @param orderBy 排序字符串
   * @return 查询构建器
   */
  def orderBy(order: String): this.type = {
    orderBy(Order.parse(order))
    this
  }

  /**
   * 指定排序字符串的位置
   *
   * @param index 从0开始
   * @param orderBy 排序字符串
   * @return 查询构建器
   */
  def orderBy(index: Int, order: String): this.type = {
    if (isNotEmpty(statement)) throw new RuntimeException("cannot add order by to a exists statement.")
    this.orders = this.orders.slice(0, index) ::: Order.parse(order) ::: this.orders.slice(index, this.orders.size)
    this
  }

  /**
   * orderBy.
   */
  def orderBy(order: Order): this.type = {
    if (null != order) orderBy(List(order))
    this
  }

  /**
   * cleanOrders.
   */
  def clearOrders(): this.type = {
    this.orders = Nil
    this
  }

  /**
   * orderBy.
   *
   * @param orders a {@link java.util.List} object.
   */
  def orderBy(orders: List[Order]): this.type = {
    if (null != orders) {
      if (isNotEmpty(statement)) throw new RuntimeException("cannot add order by to a exists statement.")
      this.orders = this.orders ::: orders
    }
    this
  }
  /**
   * groupBy.
   *
   * @param what a {@link java.lang.String} object.
   */
  def groupBy(what: String): this.type = {
    if (isNotEmpty(what)) {
      groups = groups :+ what
    }
    this
  }

  /**
   * Having subclause.
   *
   * @param what having subclause
   * @return this
   */
  def having(what: String): this.type = {
    Assert.isTrue(!groups.isEmpty)
    if (isNotEmpty(what)) having = what
    this
  }

  /**
   * 生成查询语句（如果查询语句已经存在则不进行生成）
   */
  protected def genStatement(): String = {
    if (isNotEmpty(statement)) statement
    else genQueryStatement(true)
  }

  /**
   * <p>
   * genCountStatement.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  protected def genCountStatement(): String

  /**
   * <p>
   * genQueryStatement.
   * </p>
   *
   * @param hasOrder a boolean.
   * @return a {@link java.lang.String} object.
   */
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
    if (hasOrder && !orders.isEmpty) buf.append(' ').append(Order.toSortString(orders))

    if (null != having) buf.append(" having ").append(having)
    buf.toString()
  }

}
