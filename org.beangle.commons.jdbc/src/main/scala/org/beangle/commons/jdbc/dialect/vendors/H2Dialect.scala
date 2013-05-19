/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types._

import org.beangle.commons.jdbc.dialect._

class H2Dialect extends AbstractDialect("[1.3,)") {

  protected override def buildSequenceGrammar = {
    val ss: SequenceGrammar = new SequenceGrammar()
    ss.querySequenceSql = "select sequence_name,current_value,increment,cache from information_schema.sequences where sequence_schema=':schema'"
    ss.nextValSql = "call next value for :name"
    ss.selectNextValSql = "next value for :name"
    ss.createSql = "create sequence :name start with :start increment by :increment cache :cache"
    ss.dropSql = "drop sequence if exists :name"
    ss
  }

  protected override def registerType = {
    registerType(CHAR, "char($l)")
    registerType(VARCHAR, "varchar($l)")
    registerType(LONGVARCHAR, "longvarchar")

    registerType(BOOLEAN, "Boolean")
    registerType(BIT, "bit")
    registerType(INTEGER, "integer")
    registerType(SMALLINT, "smallint")
    registerType(TINYINT, "tinyint")
    registerType(BIGINT, "bigint")
    registerType(DECIMAL, "decimal")
    registerType(DOUBLE, "double")
    registerType(FLOAT, "float")
    registerType(NUMERIC, "numeric")

    registerType(DATE, "date")
    registerType(TIME, "time")
    registerType(TIMESTAMP, "timestamp")

    registerType(BINARY, "binary")
    registerType(VARBINARY, "varbinary($l)")
    registerType(LONGVARBINARY, "longvarbinary")
    // HSQL has no Blob/Clob support .... but just put these here for now!
    registerType(BLOB, "longvarbinary")
    registerType(CLOB, "longvarchar")
  }

  protected override def buildLimitGrammar: LimitGrammarBean = new LimitGrammarBean("{} limit ?", "{} limit ? offset ?", true, false, false)

  protected override def buildTableGrammar: TableGrammarBean = {
    val bean = new TableGrammarBean()
    bean.columnComent = " comment '{}'"
    bean
  }

  override def defaultSchema = "PUBLIC"
}
