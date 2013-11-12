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

class OracleDialect() extends AbstractDialect("[10.1)") {

  registerKeywords(List("resource", "level"));

  protected override def registerType() = {
    registerType(CHAR, "char($l)");
    registerType(VARCHAR, "varchar2($l)")
    registerType(VARCHAR, 4000, "varchar2($l)");
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

  override def limitGrammar: LimitGrammar = {
    class OracleLimitGrammar extends LimitGrammarBean(null, null, true, false, true) {
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

    new OracleLimitGrammar
  }

  override def sequenceGrammar = {
    val ss = new SequenceGrammar();
    ss.querySequenceSql = "select sequence_name,last_number,increment_by,cache_size from all_sequences where sequence_owner=':schema'"
    ss.createSql = "create sequence :name increment by :increment start with :start cache :cache"
    ss.nextValSql = "select :name.nextval from dual"
    ss.selectNextValSql = ":name.nextval"
    ss
  }

  override def defaultSchema = "$user"

}
