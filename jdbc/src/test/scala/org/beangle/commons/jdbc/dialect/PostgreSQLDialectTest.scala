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

import java.sql.Types
import org.beangle.commons.jdbc.dialect.vendors.PostgreSQLDialect
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class PostgreSQLDialectTest extends FlatSpec with ShouldMatchers {

  "big number (size >=65535) in postgresql " should " trip to less 1000 size" in {
    val dialect = new PostgreSQLDialect();
    val scale = 0;
    val precision = 65535;
    val size = 65535;
    dialect.typeNames.get(Types.NUMERIC, size, precision, scale) equals "numeric(1000, 0)" should be(true)
  }
}
