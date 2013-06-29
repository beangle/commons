/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import scala.collection.JavaConversions._
import javax.sql.DataSource
import org.beangle.commons.jdbc.dialect.Dialect
import org.beangle.commons.jdbc.dialect.vendors.H2Dialect
import org.beangle.commons.jdbc.util.PoolingDataSourceFactory
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class MetadataLoaderTest extends FlatSpec with ShouldMatchers {

  "test h2 metadata loader " should "ok" in {
    val datasource: DataSource = new PoolingDataSourceFactory("org.h2.Driver",
      "jdbc:h2:/tmp/beangle;AUTO_SERVER=TRUE", "sa", "").getObject
    val dialect = new H2Dialect
    val database = new Database(datasource.getConnection().getMetaData(), dialect, null, "PUBLIC")
    database.loadTables(true)
    val tables = database.tables
    //(tables.size > 0) should be(true)
    for (table <- tables.values()) {
      val createSql = table.createSql(dialect)
      (null != createSql) should be(true)
      for (fk1 <- table.getForeignKeys) {
        (null != fk1.getAlterSql(dialect)) should be(true)
      }
    }
  }
}
