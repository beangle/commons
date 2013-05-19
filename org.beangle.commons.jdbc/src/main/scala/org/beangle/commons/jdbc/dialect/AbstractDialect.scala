/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.dialect

import org.beangle.commons.lang.Strings

abstract class AbstractDialect(versions: String) extends Dialect {
  val typeNames: TypeNames = new TypeNames()
  val ss: SequenceGrammar = buildSequenceGrammar
  val limitGrammar: LimitGrammar = buildLimitGrammar
  val tableGrammar: TableGrammar = buildTableGrammar
  val version: Dbversion = new Dbversion(versions)
  val _keywords: collection.mutable.Set[String] = collection.mutable.Set.empty[String]
  var caseSensitive: Boolean = false

  this.registerType

  def keywords = _keywords.toSet

  def sequenceGrammar = ss

  def getAddForeignKeyConstraintString(constraintName: String, foreignKey: Array[String],
                                       referencedTable: String, primaryKey: Array[String], referencesPrimaryKey: Boolean) = {
    val res: StringBuffer = new StringBuffer(30)
    res.append(" add constraInt ").append(constraintName).append(" foreign key (")
      .append(Strings.join(foreignKey, ", ")).append(") references ").append(referencedTable)
    if (!referencesPrimaryKey) {
      res.append(" (").append(Strings.join(primaryKey, ", ")).append(')')
    }
    res.toString()
  }

  override def supportsCascadeDelete = true

  def support(dbversion: String) = if (null != version) version.contains(dbversion) else false

  override def defaultSchema: String = null

  override def isCaseSensitive = caseSensitive

  def setCaseSensitive(newCaseSensitive: Boolean) = caseSensitive = newCaseSensitive

  protected def buildLimitGrammar: LimitGrammar

  protected def buildTableGrammar: TableGrammar = new TableGrammarBean()

  protected def buildSequenceGrammar: SequenceGrammar

  protected def registerType

  protected def registerKeywords(words: List[String]) = {
    for (word <- words) {
      _keywords += word.toLowerCase
      _keywords += word.toUpperCase
    }
  }

  protected def registerType(code: Int, capacity: Int, name: String) = {
    typeNames.put(code, capacity, name)
  }

  protected def registerType(code: Int, name: String) = typeNames.put(code, name)

}
