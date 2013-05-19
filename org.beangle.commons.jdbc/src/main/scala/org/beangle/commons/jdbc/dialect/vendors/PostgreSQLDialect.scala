/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types._

import org.beangle.commons.jdbc.dialect.AbstractDialect;

import org.beangle.commons.jdbc.dialect.LimitGrammarBean;
import org.beangle.commons.jdbc.dialect.SequenceGrammar;

import org.beangle.commons.jdbc.dialect.TableGrammarBean;

class PostgreSQLDialect extends AbstractDialect("[8.4)") {

  protected override def buildSequenceGrammar = {
    val ss: SequenceGrammar = new SequenceGrammar()
    ss.querySequenceSql = "select relname as sequence_name from pg_class where relkind='S'"
    ss.nextValSql = "select nextval (':name')"
    ss.selectNextValSql = "nextval (':name')"
    ss
  }

  protected override def registerType = {
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

  protected override def buildLimitGrammar = {
    new LimitGrammarBean("{} limit ?", "{} limit ? offset ?", true, false, false)
  }

  protected override def buildTableGrammar = {
    val bean: TableGrammarBean = new TableGrammarBean()
    bean.dropSql = "drop table {} cascade"
    bean
  }

  override def defaultSchema = "public"

}
