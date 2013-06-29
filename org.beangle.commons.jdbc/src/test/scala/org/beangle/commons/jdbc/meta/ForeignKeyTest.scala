/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import java.sql.Types

import org.beangle.commons.jdbc.dialect.vendors.OracleDialect
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class ForeignKeyTest extends FlatSpec with ShouldMatchers {

  "alter closuse " should "corret" in {
    val table = new Table("sys_table")
    val pk = new PrimaryKey("pk", new Column("id", Types.BIGINT))
    table.primaryKey = pk

    val tableA = new Table("sys_tableA")
    val fk = new ForeignKey("fkxyz", new Column("fkid", Types.BIGINT))
    tableA.addForeignKey(fk)
    fk.setReferencedTable(table)
    println(fk.getAlterSql(new OracleDialect()))
  }

}
