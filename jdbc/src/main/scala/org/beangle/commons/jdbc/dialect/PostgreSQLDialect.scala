/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.jdbc.dialect

import java.sql.Types._

class PostgreSQLDialect extends AbstractDialect("[8.4)") {

  protected override def registerType() = {
    registerType(CHAR, "char($l)")
    registerType(VARCHAR, "varchar($l)")
    registerType(LONGVARCHAR, "text")

    registerType(BOOLEAN, "Boolean")
    registerType(BIT, "bit")
    registerType(BIGINT, "int8")
    registerType(SMALLINT, "int2")
    registerType(TINYINT, "int2")
    registerType(INTEGER, "int4")

    registerType(FLOAT, "float4")
    registerType(DOUBLE, "float8")

    registerType(DECIMAL, "numeric($p, $s)")
    registerType(NUMERIC, 1000, "numeric($p, $s)")
    registerType(NUMERIC, Int.MaxValue, "numeric(1000, $s)")
    registerType(NUMERIC, "numeric($p, $s)")

    registerType(DATE, "date")
    registerType(TIME, "time")
    registerType(TIMESTAMP, "timestamp")

    registerType(BINARY, "bytea")
    registerType(VARBINARY, "bytea")
    registerType(LONGVARBINARY, "bytea")

    registerType(CLOB, "text")
    registerType(BLOB, "oid")
  }

  override def sequenceGrammar = {
    val ss = new SequenceGrammar()
    ss.querySequenceSql = "select relname as sequence_name from pg_class where relkind='S'"
    ss.nextValSql = "select nextval (':name')"
    ss.selectNextValSql = "nextval (':name')"
    ss
  }

  override def limitGrammar = new LimitGrammarBean("{} limit ?", "{} limit ? offset ?", true, false, false)

  override def tableGrammar = {
    val bean = new TableGrammarBean()
    bean.dropSql = "drop table {} cascade"
    bean
  }

  override def defaultSchema = "public"

}
