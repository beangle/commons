/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

import javax.sql.DataSource
import org.beangle.commons.jdbc.util.PoolingDataSourceFactory
import org.beangle.commons.jdbc.meta.Database
import org.beangle.commons.jdbc.dialect.vendors.H2Dialect

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
