/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import java.sql.Types
import org.beangle.commons.jdbc.dialect.vendors.H2Dialect
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

class TableTest extends FlatSpec with ShouldMatchers {

  "create sql" should "like this" in {
    val table = new Table("test", "user")
    val column = new Column("name", Types.VARCHAR)
    column.comment = "login name"
    table.addColumn(column)
    val pkColumn = new Column("id", Types.BIGINT)
    val pk = new PrimaryKey("pk", pkColumn)
    table.addColumn(pkColumn)
    table.primaryKey = pk
    val createSql = table.createSql(new H2Dialect())
    println(createSql)
  }

  "lowercase " should "corrent" in {
    val table = new Table("PUBLIC", "USER")
    val cloned = table.clone()
    (cloned == table) should be(false)
    cloned.lowerCase
    "USER".equals(table.name) should be(true)
    "user".equals(cloned.name) should be(true)
  }
}
