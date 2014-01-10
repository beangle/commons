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

class HSQL2Dialect extends AbstractDialect("[2.0.0,)") {

  protected override def registerType() = {
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

  override def sequenceGrammar = {
    val ss = new SequenceGrammar()
    ss.querySequenceSql = "select sequence_name,next_value,increment from information_schema.sequences where sequence_schema=':schema'"
    ss.nextValSql = "call next value for :name"
    ss.selectNextValSql = "next value for :name"
    ss.createSql = "create sequence :name start with :start increment by :increment"
    ss.dropSql = "drop sequence if exists :name"
    ss
  }

  override def limitGrammar = new LimitGrammarBean("{} limit ?", "{}  offset ? limit ?", false, false, false)

  override def tableGrammar = {
    val bean = new TableGrammarBean()
    bean.columnComent = " comment '{}'"
    bean
  }

  override def defaultSchema = "PUBLIC"

}
