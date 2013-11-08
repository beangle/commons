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
import org.beangle.commons.lang.Strings

class MySQLDialect extends AbstractDialect("[5.0,)") {

  registerKeywords(List("index", "explain"))
  caseSensitive = true;

  protected override def registerType() = {
    registerType(CHAR, "char($l)")
    registerType(VARCHAR, 255, "varchar($l)")
    registerType(VARCHAR, 65535, "varchar($l)")
    registerType(VARCHAR, "longtext")
    registerType(LONGVARCHAR, "longtext")

    registerType(BOOLEAN, "bit")
    registerType(BIT, "bit")
    registerType(BIGINT, "bigint")
    registerType(SMALLINT, "smallint")
    registerType(TINYINT, "tinyint")
    registerType(INTEGER, "integer")

    registerType(FLOAT, "float")
    registerType(DOUBLE, "double precision")

    registerType(DECIMAL, "decimal($p,$s)")
    registerType(NUMERIC, 65, "decimal($p, $s)")
    registerType(NUMERIC, Int.MaxValue, "decimal(65, $s)")
    registerType(NUMERIC, "decimal($p,$s)")

    registerType(DATE, "date")
    registerType(TIME, "time")
    registerType(TIMESTAMP, "datetime")

    registerType(BINARY, "blob")
    registerType(VARBINARY, "longblob")
    registerType(VARBINARY, 16777215, "mediumblob")
    registerType(VARBINARY, 65535, "blob")
    registerType(VARBINARY, 255, "tinyblob")
    registerType(LONGVARBINARY, "longblob")
    registerType(LONGVARBINARY, 16777215, "mediumblob")

    registerType(BLOB, "longblob")
    registerType(CLOB, "longtext")
  }

  override def limitGrammar = new LimitGrammarBean("{} limit ?", "{} limit ?, ?", false, false, false)

  override def getAddForeignKeyConstraintString(constraintName: String, foreignKey: Array[String],
    referencedTable: String, primaryKey: Array[String], referencesPrimaryKey: Boolean) = {
    val cols = Strings.join(foreignKey, ", ")
    new StringBuffer(30).append(" add index ").append(constraintName).append(" (").append(cols)
      .append("), add constraInt ").append(constraintName).append(" foreign key (").append(cols)
      .append(") references ").append(referencedTable).append(" (")
      .append(Strings.join(primaryKey, ", ")).append(')').toString()
  }

  override def tableGrammar = {
    val bean = new TableGrammarBean()
    bean.columnComent = " comment '{}'"
    bean.tableComment = " comment '{}'"
    bean
  }

  override def sequenceGrammar=null
}
