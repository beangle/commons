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

class HSQL2Dialect extends AbstractDialect("[2.0.0,)") {

  protected override def buildSequenceGrammar = {
    val ss: SequenceGrammar = new SequenceGrammar()
    ss.querySequenceSql = "select sequence_name,next_value,increment from information_schema.sequences where sequence_schema=':schema'"
    ss.nextValSql = "call next value for :name"
    ss.selectNextValSql = "next value for :name"
    ss.createSql = "create sequence :name start with :start increment by :increment"
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

    registerType(DOUBLE, "double")
    registerType(FLOAT, "float")

    registerType(DECIMAL, "decimal")
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

  protected override def buildLimitGrammar = {
    new LimitGrammarBean("{} limit ?", "{}  offset ? limit ?", false, false, false)
  }

  protected override def buildTableGrammar = {
    val bean: TableGrammarBean = new TableGrammarBean()
    bean.columnComent = " comment '{}'"
    bean
  }

  override def defaultSchema = "PUBLIC"

}
