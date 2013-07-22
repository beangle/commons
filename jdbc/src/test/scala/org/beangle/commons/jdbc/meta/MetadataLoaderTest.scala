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
