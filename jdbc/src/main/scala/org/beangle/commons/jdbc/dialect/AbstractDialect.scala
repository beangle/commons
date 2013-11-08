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

import org.beangle.commons.lang.Strings

abstract class AbstractDialect(versions: String) extends Dialect {
  val typeNames: TypeNames = new TypeNames()
  val version: Dbversion = new Dbversion(versions)
  val _keywords: collection.mutable.Set[String] = collection.mutable.Set.empty[String]
  var caseSensitive: Boolean = false

  this.registerType()

  def keywords = _keywords.toSet

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

  override def tableGrammar =  new TableGrammarBean()

  override def isCaseSensitive = caseSensitive

  def setCaseSensitive(newCaseSensitive: Boolean) = caseSensitive = newCaseSensitive

  protected def registerType(){}

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
