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

import javax.sql.DataSource
import org.beangle.commons.jdbc.util.PoolingDataSourceFactory
import org.beangle.commons.jdbc.meta.Database
import org.beangle.commons.jdbc.dialect.H2Dialect

class H2DialectTest extends DialectTestCase {

  "h2 " should "load tables and sequences" in {
    val ds: DataSource = new PoolingDataSourceFactory("org.h2.Driver",
      "jdbc:h2:/tmp/beangle;AUTO_SERVER=TRUE", "sa", "").getObject
    database = new Database(ds.getConnection().getMetaData(), new H2Dialect(), null, "PUBLIC")
    database.loadTables(false)
    database.loadSequences()
    listTableAndSequences
  }
}
