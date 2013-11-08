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

import java.sql.Types.{
  BOOLEAN,
  BIT,
  CHAR,
  INTEGER,
  SMALLINT,
  TINYINT,
  BIGINT,
  FLOAT,
  DOUBLE,
  DECIMAL,
  NUMERIC,
  DATE,
  TIME,
  TIMESTAMP,
  VARCHAR,
  LONGVARCHAR,
  BINARY,
  VARBINARY,
  LONGVARBINARY,
  BLOB,
  CLOB
}

import org.beangle.commons.jdbc.dialect.{
  HSQL2Dialect,
  H2Dialect,
  MySQLDialect,
  PostgreSQLDialect,
  DerbyDialect,
  SQLServer2005Dialect,
  OracleDialect,
  DB2Dialect
}

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DataTypeTest extends FlatSpec with ShouldMatchers {

  val types = Array(BOOLEAN, BIT, CHAR, INTEGER, SMALLINT, TINYINT, BIGINT,
    FLOAT, DOUBLE, DECIMAL, NUMERIC, DATE, TIME, TIMESTAMP, VARCHAR, LONGVARCHAR, BINARY, VARBINARY,
    LONGVARBINARY, BLOB, CLOB)

  "Any Dialect" should "Should convert types" in {
    testGetTypeName(new OracleDialect)
    testGetTypeName(new HSQL2Dialect)
    testGetTypeName(new H2Dialect)
    testGetTypeName(new MySQLDialect)
    testGetTypeName(new PostgreSQLDialect)
    testGetTypeName(new DerbyDialect)

    testGetTypeName(new DB2Dialect)
    testGetTypeName(new SQLServer2005Dialect)
    testGetTypeName(new H2Dialect)
  }

  private def testGetTypeName(dialect: Dialect) {
    for (oneType <- types) dialect.typeNames.get(oneType)
  }
}
