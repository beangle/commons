/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect.vendors

import java.sql.Types._
import org.beangle.commons.jdbc.dialect.AbstractDialect
import org.beangle.commons.jdbc.dialect.LimitGrammarBean
import org.beangle.commons.jdbc.dialect.TableGrammarBean;
import org.beangle.commons.lang.Strings

class MySQLDialect extends AbstractDialect("[5.0,)") {

  registerKeywords(List("index", "explain"))
  caseSensitive = true;

  protected override def buildSequenceGrammar = null

  protected override def registerType = {
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

  protected override def buildLimitGrammar = {
    new LimitGrammarBean("{} limit ?", "{} limit ?, ?", false, false, false)
  }

  override def getAddForeignKeyConstraintString(constraintName: String, foreignKey: Array[String],
                                                referencedTable: String, primaryKey: Array[String], referencesPrimaryKey: Boolean) = {
    val cols = Strings.join(foreignKey, ", ")
    new StringBuffer(30).append(" add index ").append(constraintName).append(" (").append(cols)
      .append("), add constraInt ").append(constraintName).append(" foreign key (").append(cols)
      .append(") references ").append(referencedTable).append(" (")
      .append(Strings.join(primaryKey, ", ")).append(')').toString()
  }

  protected override def buildTableGrammar = {
    val bean: TableGrammarBean = new TableGrammarBean()
    bean.columnComent = " comment '{}'"
    bean.tableComment = " comment '{}'"
    bean
  }

}
