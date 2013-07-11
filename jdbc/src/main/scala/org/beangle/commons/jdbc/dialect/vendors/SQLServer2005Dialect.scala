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
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types._

import org.beangle.commons.jdbc.dialect.AbstractDialect;

import org.beangle.commons.jdbc.dialect.LimitGrammarBean;

class SQLServer2005Dialect extends AbstractDialect("[2005,2008)") {

  private val SELECT: String = "select"
  private val FROM: String = "from"
  private val DISTINCT: String = "distinct"

  protected override def buildSequenceGrammar = null

  protected override def registerType = {
    registerType(CHAR, "char($l)")
    registerType(VARCHAR, "varchar($l)")
    registerType(LONGVARCHAR, "text")

    registerType(BIT, "tinyint")
    registerType(BOOLEAN, "tinyint")
    registerType(BIGINT, "numeric(19,0)")
    registerType(SMALLINT, "smallint")
    registerType(TINYINT, "tinyint")
    registerType(INTEGER, "int")
    registerType(FLOAT, "float")
    registerType(DECIMAL, "double precision")
    registerType(DOUBLE, "double precision")
    registerType(NUMERIC, "numeric($p,$s)")

    registerType(DATE, "datetime")
    registerType(TIME, "datetime")
    registerType(TIMESTAMP, "datetime")

    registerType(BINARY, "binary")
    registerType(VARBINARY, "varbinary($l)")
    registerType(LONGVARBINARY, "varbinary($l)")
    registerType(BLOB, "image")
    registerType(CLOB, "text")
  }

  protected override def buildLimitGrammar = {
    class SqlServer2005LimitGrammar(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
      bindFirst: Boolean, useMax: Boolean) extends LimitGrammarBean(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
      bindFirst: Boolean, useMax: Boolean) {
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
    new SqlServer2005LimitGrammar(null, null, false, false, true);
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
