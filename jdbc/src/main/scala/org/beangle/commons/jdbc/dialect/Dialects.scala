/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.jdbc.dialect

import java.lang.reflect.Constructor
import org.beangle.commons.lang.Strings

object Dialects {

  def HSQL2: String = "HSQL2"

  def H2: String = "H2"

  def MySQL: String = "MySQL"

  def Oracle: String = "Oracle"

  def DB2: String = "DB2"

  def PostgreSQL: String = "PostgreSQL"

  def SQLServer2005: String = "SQLServer2005"

  var constructors: Map[String, Constructor[_ <: Dialect]] = Map.empty

  def getDialect(dialectName: String): Dialect = {
    val con: Constructor[_ <: Dialect] = constructors.get(dialectName).orNull
    if (null == con) {
      throw new RuntimeException(dialectName + " not supported")
    } else {
      return con.newInstance()
    }
  }

  def register(clazz: Class[_ <: Dialect]) {
    val name: String = Strings.substringBefore(clazz.getSimpleName(), "Dialect")
    constructors += (name -> clazz.getConstructor())
  }

  def register(shortname: String, clazz: Class[_ <: Dialect]) {
    constructors += (shortname -> clazz.getConstructor())
  }

  register(classOf[DB2Dialect])
  register(classOf[DerbyDialect])
  register(classOf[H2Dialect])
  register(classOf[HSQL2Dialect])
  register(classOf[MySQLDialect])
  register(classOf[OracleDialect])
  register(classOf[PostgreSQLDialect])
  register(classOf[SQLServerDialect])
  register(classOf[SQLServer2005Dialect])
  register(classOf[SQLServer2008Dialect])

  private def printPad(name: String) { print(Strings.rightPad(name, 17, ' ')) }

  def printTypeMatrix() {
    import java.sql.Types._
    val types = Array(BOOLEAN, BIT, CHAR, INTEGER, SMALLINT, TINYINT, BIGINT,
      FLOAT, DOUBLE, DECIMAL, NUMERIC, DATE, TIME, TIMESTAMP, VARCHAR, LONGVARCHAR,
      BINARY, VARBINARY, LONGVARBINARY, BLOB, CLOB)

    val typeNames = Array("BOOLEAN", "BIT", "CHAR", "INTEGER", "SMALLINT", "TINYINT", "BIGINT",
      "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC", "DATE", "TIME", "TIMESTAMP", "VARCHAR", "LONGVARCHAR",
      "BINARY", "VARBINARY", "LONGVARBINARY", "BLOB", "CLOB")

    val dialects = Array(new OracleDialect, new H2Dialect, new MySQLDialect, new PostgreSQLDialect,
      new SQLServer2005Dialect, new DB2Dialect)

    printPad("Type/Dialect")
    for (dialect <- dialects) {
      printPad(Strings.replace(dialect.getClass.getSimpleName, "Dialect", ""))
    }

    println()
    for (i <- 0 until types.length) {
      printPad(typeNames(i))
      for (dialect <- dialects) {
        var typeName = "error"
        try {
          typeName = dialect.typeNames.get(types(i))
        } catch {
          case e: Exception =>
        }
        printPad(typeName)
      }
      println("")
    }
  }
}

abstract class Dialect {

  def tableGrammar: TableGrammar

  def limitGrammar: LimitGrammar

  def sequenceGrammar: SequenceGrammar

  def defaultSchema: String

  def typeNames: TypeNames

  def keywords: Set[String]

  def supportsCascadeDelete: Boolean

  def isCaseSensitive: Boolean

  def getAddForeignKeyConstraintString(constraintName: String, foreignKey: Array[String],
    referencedTable: String, primaryKey: Array[String], referencesPrimaryKey: Boolean): String

}
