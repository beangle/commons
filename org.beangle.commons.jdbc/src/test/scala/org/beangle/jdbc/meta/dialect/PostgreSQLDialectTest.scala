/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.jdbc.meta.dialect

import java.sql.Types
import org.beangle.jdbc.meta.dialect.vendors.PostgreSQLDialect
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

class PostgreSQLDialectTest extends FlatSpec with ShouldMatchers {

  "big number (size >=65535) in postgresql " should " trip to less 1000 size" in {
    val dialect = new PostgreSQLDialect();
    val scale = 0;
    val precision = 65535;
    val size = 65535;
    dialect.typeNames.get(Types.NUMERIC, size, precision, scale) equals "numeric(1000, 0)" should be(true)
  }
}
