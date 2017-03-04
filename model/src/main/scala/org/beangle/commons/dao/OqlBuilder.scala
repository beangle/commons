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
package org.beangle.commons.dao

import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings._
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.orm.Jpas

object OqlBuilder {
  val Lang = Query.Lang("Oql")

  def oql[E](oql: String): OqlBuilder[E] = {
    val query = new OqlBuilder[E]()
    query.statement = oql
    query
  }

  def from[E](from: String): OqlBuilder[E] = {
    val query = new OqlBuilder[E]()
    query.newFrom(from)
    query
  }

  def from[E](entityName: String, alias: String): OqlBuilder[E] = {
    val query = new OqlBuilder[E]()
    query.entityClass = ClassLoaders.load(entityName).asInstanceOf[Class[E]]
    query.alias = alias
    query.select = "select " + alias
    query.from = concat("from ", entityName, " ", alias)
    query
  }

  def from[E](entityClass: Class[E]): OqlBuilder[E] = {
    from(entityClass, uncapitalize(substringAfterLast(Jpas.findEntityName(entityClass), ".")))
  }

  def from[E](entityClass: Class[E], alias: String): OqlBuilder[E] = {
    val query = new OqlBuilder[E]()
    query.entityClass = entityClass
    query.alias = alias
    query.select = "select " + alias
    query.from = concat("from ", Jpas.findEntityName(entityClass), " ", alias)
    query
  }

}
/**
 * 实体类查询 Object Query Language Builder
 *
 * @author chaostone
 */
class OqlBuilder[T] private () extends AbstractQueryBuilder[T] {

  /** 查询实体类 */
  var entityClass: Class[T] = _

  /**
   * 形成计数查询语句，如果不能形成，则返回""
   */
  protected def genCountStatement(): String = {
    val countString = new StringBuilder("select count(*) ")
    // 原始查询语句
    val genQueryStr = genQueryStatement(false)
    if (isEmpty(genQueryStr)) { return "" }
    val lowerCaseQueryStr = genQueryStr.toLowerCase()

    if (contains(lowerCaseQueryStr, " group ")) { return "" }
    if (contains(lowerCaseQueryStr, " union ")) { return "" }

    val indexOfFrom = findIndexOfFrom(lowerCaseQueryStr)
    val selectWhat = lowerCaseQueryStr.substring(0, indexOfFrom)
    val indexOfDistinct = selectWhat.indexOf("distinct")
    // select distinct a from table
    if (-1 != indexOfDistinct) {
      if (contains(selectWhat, ",")) {
        return ""
      } else {
        countString.clear()
        countString.append("select count(")
        countString.append(genQueryStr.substring(indexOfDistinct, indexOfFrom)).append(") ")
      }
    }

    var orderIdx = genQueryStr.lastIndexOf(" order ")
    if (-1 == orderIdx) orderIdx = genQueryStr.length()
    countString.append(genQueryStr.substring(indexOfFrom, orderIdx))
    countString.toString()
  }

  /**
   * Find index of from
   *
   * @param query
   * @return -1 or from index
   */
  private def findIndexOfFrom(query: String): Int = {
    if (query.startsWith("from")) return 0
    var fromIdx = query.indexOf(" from ")
    if (-1 == fromIdx) return -1
    val first = query.substring(0, fromIdx).indexOf("(")
    if (first > 0) {
      var leftCnt = 1
      var i = first + 1
      while (leftCnt != 0 && i < query.length) {
        if (query.charAt(i) == '(') leftCnt += 1
        else if (query.charAt(i) == ')') leftCnt -= 1
        i += 1
      }
      if (leftCnt > 0) return -1
      else {
        fromIdx = query.indexOf(" from ", i)
        return if (fromIdx == -1) -1 else fromIdx + 1
      }
    } else {
      return fromIdx + 1
    }
  }

  def forEntity(entityClass: Class[T]): this.type = {
    this.entityClass = entityClass
    this
  }

  override def lang = OqlBuilder.Lang
}
