/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General def License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.jdbc.meta.dialect

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

import org.beangle.jdbc.meta.dialect.vendors.{
  HSQL2Dialect,
  H2Dialect,
  MySQLDialect,
  PostgreSQLDialect,
  DerbyDialect,
  SQLServer2005Dialect,
  OracleDialect,
  DB2Dialect
}

class DataTypeTest {

  val types = Array(BOOLEAN, BIT, CHAR, INTEGER, SMALLINT, TINYINT, BIGINT,
    FLOAT, DOUBLE, DECIMAL, NUMERIC, DATE, TIME, TIMESTAMP, VARCHAR, LONGVARCHAR, BINARY, VARBINARY,
    LONGVARBINARY, BLOB, CLOB);

  def testHSQL2() {
    val dialect: Dialect = new HSQL2Dialect();
    testGetTypeName(dialect);
  }

  def testH2() {
    val dialect: Dialect = new H2Dialect();
    testGetTypeName(dialect);
  }

  def testMySQL() {
    val dialect: Dialect = new MySQLDialect();
    testGetTypeName(dialect);
  }

  def testPostgreSQL() {
    val dialect: Dialect = new PostgreSQLDialect();
    testGetTypeName(dialect);
  }

  def testDerby() {
    val dialect: Dialect = new DerbyDialect();
    testGetTypeName(dialect);
  }

  // test commacial jdbc--------------------------------------------
  def testOracle() {
    val dialect: Dialect = new OracleDialect();
    testGetTypeName(dialect);
  }

  def testDb2() {
    val dialect: Dialect = new DB2Dialect();
    testGetTypeName(dialect);
  }

  def testSQLServer2005() {
    val dialect: Dialect = new SQLServer2005Dialect();
    testGetTypeName(dialect);
  }

  private def testGetTypeName(dialect: Dialect) = {
    for (oneType <- types) {
      assert(null != dialect.typeNames.get(oneType))
    }
  }
}
