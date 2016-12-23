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
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Strings

/**
 * SqlBuilder
 * @author chaostone
 */
object SqlBuilder {

  def sql(queryStr: String): SqlBuilder = {
    val sqlQuery = new SqlBuilder()
    sqlQuery.statement = queryStr
    sqlQuery
  }
  val Lang = Query.Lang("Sql")
}

class SqlBuilder extends AbstractQueryBuilder[Array[Any]] {

  protected def genCountStatement() = "select count(*) from (" + genQueryStatement(false) + ")"

  override def lang = SqlBuilder.Lang
}
