/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.jdbc

import java.sql.Types
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostgreSQLEngineTest extends FlatSpec with Matchers {

  "big number (size >=65535) in postgresql " should " trip to less 1000 size" in {
    val dialect = Engines.PostgreSQL
    val scale = 0;
    val precision = 65535
    val size = 65535;
    dialect.typeNames.get(Types.NUMERIC, size, precision, scale) equals "numeric(1000, 0)" should be(true)
    dialect.typeNames.get(Types.DECIMAL, 1, 1, 0) equals "boolean" should be(true)
  }
}
