/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types

import org.beangle.commons.jdbc.dialect.AbstractDialect
import org.beangle.commons.jdbc.dialect.LimitGrammarBean
import org.beangle.commons.jdbc.dialect.SequenceGrammar

class DB2Dialect extends AbstractDialect("[8.0]") {

  protected override def buildSequenceGrammar = {
    val ss: SequenceGrammar = new SequenceGrammar()
    ss.querySequenceSql = "select name as sequence_name,start-1 as current_value,increment,cache from sysibm.syssequences where schema=':schema'"
    ss.nextValSql = "values nextval for :name"
    ss.dropSql = "drop sequence :name restrict"
    ss.selectNextValSql = "nextval for :name"
    ss
  }

  protected override def registerType = {
    registerType(Types.BOOLEAN, "smallint")
    registerType(Types.BIT, "smallint")
    registerType(Types.DECIMAL, "bigint")
    registerType(Types.BIGINT, "bigint")
    registerType(Types.SMALLINT, "smallint")
    registerType(Types.TINYINT, "smallint")
    registerType(Types.INTEGER, "integer")
    
    registerType(Types.CHAR, "char($l)")
    registerType(Types.VARCHAR, "varchar($l)")
    
    registerType(Types.FLOAT, "float")
    registerType(Types.DOUBLE, "double")
    registerType(Types.DATE, "date")
    registerType(Types.TIME, "time")
    registerType(Types.TIMESTAMP, "timestamp")
    registerType(Types.NUMERIC, "numeric($p,$s)")
    registerType(Types.BLOB, "blob($l)")
    registerType(Types.CLOB, "clob($l)")

    registerType(Types.VARBINARY, "varchar($l) for bit data")
    // FIXME correct definition needed!
    registerType(Types.LONGVARCHAR, "varchar($l)")
    registerType(Types.BINARY, "raw")
    registerType(Types.LONGVARBINARY, "raw")
  }

  /**
   * Render the <tt>rownumber() over ( .... ) as rownumber_,</tt> bit, that
   * goes in the select list
   */
  private def getRowNumber(sql: String) = {
    val rownumber: StringBuilder = new StringBuilder(50).append("rownumber() over(")
    val orderByIndex: Int = sql.toLowerCase().indexOf("order by")
    if (orderByIndex > 0 && !hasDistinct(sql)) {
      rownumber.append(sql.substring(orderByIndex))
    }
    rownumber.append(") as rownumber_,")
    rownumber.toString()
  }

  private def hasDistinct(sql: String) = sql.toLowerCase().indexOf("select distinct") >= 0

  protected override def buildLimitGrammar = {
    class DB2LimitGrammar(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
                          bindFirst: Boolean, useMax: Boolean) extends LimitGrammarBean(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
      bindFirst: Boolean, useMax: Boolean) {
      override def limit(sql: String, hasOffset: Boolean) = {
        val startOfSelect = sql.toLowerCase().indexOf("select")
        val pagingSelect: StringBuilder = new StringBuilder(sql.length() + 100)
          .append(sql.substring(0, startOfSelect)) // add the comment
          .append("select * from ( select ") // nest the main query in an
          // outer select
          .append(getRowNumber(sql)) // add the rownnumber bit into the
        // outer query select list

        if (hasDistinct(sql)) {
          // add another (inner) nested select
          pagingSelect.append(" row_.* from ( ")
            // add the main query
            .append(sql.substring(startOfSelect))
            // close off the inner nested select
            .append(" ) as row_")
        } else {
          // add the main query
          pagingSelect.append(sql.substring(startOfSelect + 6))
        }

        pagingSelect.append(" ) as temp_ where rownumber_ ")

        // add the restriction to the outer select
        if (hasOffset) {
          pagingSelect.append("between ?+1 and ?")
        } else {
          pagingSelect.append("<= ?")
        }
        pagingSelect.toString()
      }
    }
    new DB2LimitGrammar(null, null, false, false, true)
  }

  override def defaultSchema(): String = null
}
