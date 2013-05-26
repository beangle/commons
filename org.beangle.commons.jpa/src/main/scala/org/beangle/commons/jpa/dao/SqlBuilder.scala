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

import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.entity.dao.Query
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings

object SqlBuilder{
  /**
   * sql.
   */
  def sql(queryStr:String ) :SqlBuilder={
    val sqlQuery = new SqlBuilder()
    sqlQuery.statement = queryStr
    sqlQuery
  }
  val Lang = Query.Lang("Sql")
}
/**
 * sql查询
 * 
 * @author chaostone
 * @version $Id: $
 */
class SqlBuilder extends AbstractQueryBuilder[Array[Any]] {

  /**
   * genCountStatement.
   */
  protected def genCountStatement() =  "select count(*) from (" + genQueryStatement(false) + ")"

  override  def lang= SqlBuilder.Lang
}
