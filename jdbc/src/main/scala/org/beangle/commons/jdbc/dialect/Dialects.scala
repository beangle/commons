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
