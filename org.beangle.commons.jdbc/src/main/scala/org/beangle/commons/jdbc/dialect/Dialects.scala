/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

import java.lang.reflect.Constructor
import java.util.Map
import org.beangle.commons.jdbc.dialect.vendors._
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings

object Dialects {

  def HSQL2: String = "HSQL2"

  def H2: String = "H2"

  def MySQL: String = "MySQL"

  def Oracle: String = "Oracle"

  def DB2: String = "DB2"

  def PostgreSQL: String = "PostgreSQL"

  def SQLServer2005: String = "SQLServer2005"

  val constructors = CollectUtils.newHashMap[String, Constructor[_ <: Dialect]] 

  def getDialect(dialectName: String): Dialect = {
    val con: Constructor[_ <: Dialect] = constructors.get(dialectName)
    if (null == con) {
      throw new RuntimeException(dialectName + " not supported")
    } else {
      return con.newInstance()
    }
  }

  def register(clazz: Class[_ <: Dialect]) = {
    val name: String = Strings.substringBefore(clazz.getSimpleName(),"Dialect")
    val con: Constructor[_ <: Dialect] = clazz.getConstructor()
    constructors.put(name, con)
  }

  def register(shortname: String, clazz: Class[_ <: Dialect]) = {
    val con: Constructor[_ <: Dialect] = clazz.getConstructor()
    constructors.put(shortname, con)
  }

  register(classOf[DB2Dialect])
  register(classOf[DerbyDialect])
  register(classOf[H2Dialect])
  register(classOf[HSQL2Dialect])
  register(classOf[MySQLDialect])
  register(classOf[OracleDialect])
  register(classOf[PostgreSQLDialect])
  register(classOf[SQLServer2005Dialect])
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
