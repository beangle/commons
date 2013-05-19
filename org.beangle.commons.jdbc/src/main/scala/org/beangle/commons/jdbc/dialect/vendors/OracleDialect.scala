/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types._

import org.beangle.commons.jdbc.dialect._;

class OracleDialect() extends AbstractDialect("[10.1)") {

  registerKeywords(List("resource", "level"));

  protected override def buildSequenceGrammar = {
    val ss: SequenceGrammar = new SequenceGrammar();
    ss.querySequenceSql = "select sequence_name,last_number,increment_by,cache_size from all_sequences where sequence_owner=':schema'"
    ss.createSql = "create sequence :name increment by :increment start with :start cache :cache"
    ss.nextValSql = "select :name.nextval from dual"
    ss.selectNextValSql = ":name.nextval"
    ss
  }

  protected override def registerType = {
    registerType(CHAR, "char($l)");
    registerType(VARCHAR, 4000, "varchar2($l)" );
    registerType(LONGVARCHAR, "long");

    registerType(BOOLEAN, "number(1,0)");
    registerType(BIT, "number(1,0)");
    registerType(SMALLINT, "number(5,0)");
    registerType(TINYINT, "number(3,0)");
    registerType(INTEGER, "number(10,0)");
    registerType(BIGINT, "number(19,0)");

    registerType(FLOAT, "float");
    registerType(DOUBLE, "double precision");

    registerType(DECIMAL, "number($p,$s)");
    registerType(NUMERIC, "number($p,$s)");
    registerType(NUMERIC, 38, "number($p,$s)");
    registerType(NUMERIC, Int.MaxValue, "number(38,$s)");

    registerType(DATE, "date");
    registerType(TIME, "date");
    registerType(TIMESTAMP, "date");

    registerType(BINARY, "raw");
    registerType(VARBINARY, 2000, "raw($l)");
    registerType(VARBINARY, "long raw");
    registerType(LONGVARBINARY, "long raw");

    registerType(BLOB, "blob");
    registerType(CLOB, "clob");
  }

  protected override def buildLimitGrammar: LimitGrammar = {
    class OracleLimitGrammar(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
                             bindFirst: Boolean, useMax: Boolean) extends LimitGrammarBean(pattern: String, offsetPattern: String, bindInReverseOrder: Boolean,
      bindFirst: Boolean, useMax: Boolean) {
      override def limit(sqlStr: String, hasOffset: Boolean) = {
        var sql = sqlStr.trim();
        var isForUpdate = false;
        if (sql.toLowerCase().endsWith(" for update")) {
          sql = sql.substring(0, sql.length() - 11);
          isForUpdate = true;
        }
        val pagingSelect: StringBuilder = new StringBuilder(sql.length() + 100);
        if (hasOffset) {
          pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
        } else {
          pagingSelect.append("select * from ( ");
        }
        pagingSelect.append(sql);
        if (hasOffset) {
          pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
        } else {
          pagingSelect.append(" ) where rownum <= ?");
        }

        if (isForUpdate) {
          pagingSelect.append(" for update");
        }
        pagingSelect.toString()
      }
    }

    new OracleLimitGrammar(null, null, true, false, true)
  }

}
