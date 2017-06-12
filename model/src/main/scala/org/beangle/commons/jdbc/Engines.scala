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

import java.sql.Types._

object Engines {

  private val name2Engines =
    Map("PostgreSQL" -> PostgreSQL, "MySQL" -> MySQL, "H2" -> H2,
      "HSQL Database Engine" -> HSQL, "Oracle" -> Oracle)

  def forDatabase(databaseName: String): Engine = {
    name2Engines.get(databaseName) match {
      case Some(engine) => engine
      case None =>
        if (databaseName.startsWith("DB2/")) {
          DB2
        } else if (databaseName.startsWith("Microsoft SQL Server")) {
          SQLServer
        }
        throw new RuntimeException(s"Cannot find engine for database $databaseName")
    }
  }

  object PostgreSQL extends AbstractEngine {
    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar($l)", LONGVARCHAR -> "text",
      BOOLEAN -> "boolean", BIT -> "boolean",
      SMALLINT -> "int2", TINYINT -> "int2", INTEGER -> "int4", BIGINT -> "int8",
      FLOAT -> "float4", REAL -> "float4", DOUBLE -> "float8",
      DECIMAL -> "numeric($p,$s)", NUMERIC -> "numeric($p,$s)",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "timestamp",
      BINARY -> "bytea", VARBINARY -> "bytea", LONGVARBINARY -> "bytea",
      CLOB -> "text", BLOB -> "oid")

    registerTypes2(
      (DECIMAL, 1, "boolean"), (DECIMAL, 10, "integer"),
      (DECIMAL, 19, "bigint"), (NUMERIC, 1000, "numeric($p, $s)"),
      (NUMERIC, Int.MaxValue, "numeric(1000, $s)"))

    override def storeCase: StoreCase.Value = {
      StoreCase.Lower
    }

    override def toType(sqlCode: Int, length: Int, precision: Int, scale: Int): SqlType = {
      if (sqlCode == DECIMAL) {
        val result = precision match {
          case 1  => new SqlType(BOOLEAN, "boolean")
          case 5  => new SqlType(SMALLINT, "int2")
          case 10 => new SqlType(INTEGER, "int4")
          case 19 => new SqlType(BIGINT, "int8")
          case _  => super.toType(sqlCode, length, scale)
        }
        result.length = Some(length)
        result
      } else super.toType(sqlCode, length, precision, scale)
    }
  }

  object MySQL extends AbstractEngine {
    override def quoteChars: Tuple2[Char, Char] = {
      ('`', '`')
    }

    registerKeywords("index", "explain")

    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "longtext", LONGVARCHAR -> "longtext",
      BOOLEAN -> "bit", BIT -> "bit",
      TINYINT -> "tinyint", SMALLINT -> "smallint", INTEGER -> "integer", BIGINT -> "bigint",
      FLOAT -> "float", DOUBLE -> "double precision",
      DECIMAL -> "decimal($p,$s)", NUMERIC -> "decimal($p,$s)",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "datetime",
      BINARY -> "binary($l)", VARBINARY -> "longblob", LONGVARBINARY -> "longblob",
      BLOB -> "longblob", CLOB -> "longtext", NCLOB -> "longtext")

    registerTypes2(
      (VARCHAR, 65535, "varchar($l)"),
      (NUMERIC, 65, "decimal($p, $s)"),
      (NUMERIC, Int.MaxValue, "decimal(65, $s)"),
      (VARBINARY, 255, "tinyblob"),
      (VARBINARY, 65535, "blob"),
      (VARBINARY, 16777215, "mediumblob"),
      (LONGVARBINARY, 16777215, "mediumblob"))
  }

  object Oracle extends AbstractEngine {
    registerKeywords("resource", "level")

    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar2($l)", LONGVARCHAR -> "long",
      BOOLEAN -> "number(1,0)", BIT -> "number(1,0)",
      SMALLINT -> "number(5,0)", TINYINT -> "number(3,0)", INTEGER -> "number(10,0)", BIGINT -> "number(19,0)",
      FLOAT -> "float", REAL -> "float", DOUBLE -> "double precision",
      DECIMAL -> "number($p,$s)", NUMERIC -> "number($p,$s)",
      DATE -> "date", TIME -> "date", TIMESTAMP -> "date",
      BINARY -> "raw", VARBINARY -> "long raw", LONGVARBINARY -> "long raw",
      BLOB -> "blob", CLOB -> "clob")

    registerTypes2(
      (VARCHAR, 4000, "varchar2($l)"), (NUMERIC, 38, "number($p,$s)"),
      (NUMERIC, Int.MaxValue, "number(38,$s)"), (VARBINARY, 2000, "raw($l)"))

    override def storeCase: StoreCase.Value = {
      StoreCase.Upper
    }
  }

  object DB2 extends AbstractEngine {
    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar($l)",
      BOOLEAN -> "smallint", BIT -> "smallint",
      SMALLINT -> "smallint", TINYINT -> "smallint", INTEGER -> "integer", DECIMAL -> "bigint", BIGINT -> "bigint",
      FLOAT -> "float", DOUBLE -> "double", NUMERIC -> "numeric($p,$s)",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "timestamp",
      BINARY -> "varchar($l) for bit data",
      VARBINARY -> "varchar($l) for bit data",
      LONGVARCHAR -> "long varchar",
      LONGVARBINARY -> "long varchar for bit data",
      BLOB -> "blob($l)", CLOB -> "clob($l)")
  }

  object H2 extends AbstractEngine {
    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar($l)", LONGVARCHAR -> "longvarchar",
      BOOLEAN -> "Boolean", BIT -> "bit",
      TINYINT -> "tinyint", SMALLINT -> "smallint", INTEGER -> "integer", BIGINT -> "bigint",
      FLOAT -> "float", DOUBLE -> "double",
      DECIMAL -> "decimal", NUMERIC -> "numeric",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "timestamp",
      BINARY -> "binary", VARBINARY -> "varbinary($l)", LONGVARBINARY -> "longvarbinary",
      BLOB -> "longvarbinary", CLOB -> "longvarchar")

    override def storeCase: StoreCase.Value = {
      StoreCase.Upper
    }
  }

  object HSQL extends AbstractEngine {
    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar($l)", LONGVARCHAR -> "longvarchar",
      BOOLEAN -> "Boolean", BIT -> "bit",
      TINYINT -> "tinyint", SMALLINT -> "smallint", INTEGER -> "integer", BIGINT -> "bigint",
      FLOAT -> "float", DOUBLE -> "double",
      DECIMAL -> "decimal", NUMERIC -> "numeric",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "timestamp",
      BINARY -> "binary", VARBINARY -> "varbinary($l)", LONGVARBINARY -> "longvarbinary",
      BLOB -> "longvarbinary", CLOB -> "longvarchar")

    override def storeCase: StoreCase.Value = {
      StoreCase.Upper
    }
  }

  object SQLServer extends AbstractEngine {
    override def quoteChars: Tuple2[Char, Char] = {
      ('[', ']')
    }

    registerTypes(
      CHAR -> "char($l)", VARCHAR -> "varchar(MAX)", NVARCHAR -> "nvarchar(MAX)",
      BIT -> "bit", BOOLEAN -> "bit",
      TINYINT -> "smallint", SMALLINT -> "smallint", INTEGER -> "int", BIGINT -> "bigint",
      FLOAT -> "float", DOUBLE -> "double precision",
      DECIMAL -> "double precision", NUMERIC -> "numeric($p,$s)",
      DATE -> "date", TIME -> "time", TIMESTAMP -> "datetime2",
      BINARY -> "binary", VARBINARY -> "varbinary(MAX)",
      LONGVARCHAR -> "text", LONGVARBINARY -> "varbinary(MAX)",
      BLOB -> "varbinary(MAX)", CLOB -> "varchar(MAX)");

    registerTypes2(
      (VARCHAR, 8000, "varchar($l)"),
      (VARBINARY, 8000, "varbinary($l)"),
      (NVARCHAR, 4000, "nvarchar($l)"))
  }
}
