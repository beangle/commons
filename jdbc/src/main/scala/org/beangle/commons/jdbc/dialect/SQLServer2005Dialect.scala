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
package org.beangle.commons.jdbc.dialect

import java.sql.Types._

class SQLServer2005Dialect(version: String) extends SQLServerDialect(version) {

  def this() {
    this("[2005,2008)")
  }

  protected override def registerType() = {
    super.registerType()
    registerType(VARCHAR, 8000, "varchar($l)")
    registerType(VARCHAR, "varchar(MAX)")

    registerType(BIGINT, "bigint")

    registerType(VARBINARY, "varbinary(MAX)")
    registerType(LONGVARBINARY, "varbinary(MAX)")
    registerType(BLOB, "varbinary(MAX)")
    registerType(CLOB, "varchar(MAX)")
  }

  override def limitGrammar: LimitGrammar = {
    class SqlServerLimitGrammar extends LimitGrammarBean(null, null, false, false, true) {
      override def limit(querySqlString: String, hasOffset: Boolean) = {
        val sb: StringBuilder = new StringBuilder(querySqlString.trim().toLowerCase())

        val orderByIndex: Int = sb.indexOf("order by")
        var orderby: CharSequence = "ORDER BY CURRENT_TIMESTAMP";
        if (orderByIndex > 0) orderby = sb.subSequence(orderByIndex, sb.length())

        // Delete the order by clause at the end of the query
        if (orderByIndex > 0) {
          sb.delete(orderByIndex, orderByIndex + orderby.length())
        }

        // HHH-5715 bug fix
        replaceDistinctWithGroupBy(sb)

        insertRowNumberFunction(sb, orderby)

        // Wrap the query within a with statement:
        sb.insert(0, "WITH query AS (").append(") SELECT * FROM query ")
        sb.append("WHERE __hibernate_row_nr__ BETWEEN ? AND ?")

        sb.toString()
      }
    }
    new SqlServerLimitGrammar
  }

  protected def replaceDistinctWithGroupBy(sql: StringBuilder) = {
    val distinctIndex = sql.indexOf(DISTINCT)
    if (distinctIndex > 0) {
      sql.delete(distinctIndex, distinctIndex + DISTINCT.length() + 1)
      sql.append(" group by").append(getSelectFieldsWithoutAliases(sql))
    }
  }

  protected def insertRowNumberFunction(sql: StringBuilder, orderby: CharSequence) {
    // Find the end of the select statement
    val selectEndIndex = sql.indexOf(SELECT) + SELECT.length()
    // Insert after the select statement the row_number() function:
    sql.insert(selectEndIndex, " ROW_NUMBER() OVER (" + orderby + ") as __hibernate_row_nr__,")
  }

  protected def getSelectFieldsWithoutAliases(sql: StringBuilder) = {
    val select = sql.substring(sql.indexOf(SELECT) + SELECT.length(), sql.indexOf(FROM))
    // Strip the as clauses
    stripAliases(select)
  }

  protected def stripAliases(str: String) = str.replaceAll("\\sas[^,]+(,?)", "$1")
}
