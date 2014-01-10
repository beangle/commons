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
package org.beangle.commons.jdbc.meta

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.beangle.commons.jdbc.dialect.Dialect
/**
 * JDBC table metadata
 *
 * @author chaostone
 */
class Table(var name: String) extends Comparable[Table] with Cloneable {
  var schema: String = null
  var primaryKey: PrimaryKey = null
  var comment: String = null
  val columns = new ListBuffer[Column]
  val uniqueKeys = new ListBuffer[UniqueKey]
  val foreignKeys = new ListBuffer[ForeignKey]
  val indexes = new ListBuffer[Index]

  def this(schema: String, name: String) {
    this(name)
    this.schema = schema
  }

  def columnNames: List[String] = columns.result.map(_.name)

  def identifier = Table.qualify(schema, name)

  def identifier(givenSchema: String) = {
    if (null == givenSchema || givenSchema.isEmpty()) name
    else Table.qualify(givenSchema, name)
  }

  def getOrCreateUniqueKey(keyName: String) = {
    var uk: UniqueKey = uniqueKeys.find(f => f.name.equals(keyName)).orNull
    if (uk == null) {
      uk = new UniqueKey(keyName)
      uk.table = this
      uniqueKeys += uk
    }
    uk
  }

  def insertSql: String = {
    val sb = new StringBuilder("insert into ")
    sb ++= identifier(schema)
    sb += '('
    sb ++= columnNames.mkString(",")
    sb ++= ") values("
    sb ++= ("?," * columns.size)
    sb.setCharAt(sb.length() - 1, ')')
    sb.mkString
  }

  /**
   * @param dialect
   * @return
   */
  def createSql(dialect: Dialect): String = {
    val grammar = dialect.tableGrammar
    val buf = new StringBuilder(grammar.createString).append(' ').append(identifier(schema)).append(" (")
    val iter: Iterator[Column] = columns.iterator
    val l = columns.toList
    while (iter.hasNext) {
      val col: Column = iter.next()
      buf.append(col.name).append(' ')
      buf.append(col.getSqlType(dialect))

      val defaultValue: String = col.defaultValue
      if (defaultValue != null) buf.append(" default ").append(defaultValue)

      if (col.nullable) {
        buf.append(grammar.nullColumnString)
      } else {
        buf.append(" not null")
      }
      val useUniqueConstraint = col.unique && (!col.nullable || grammar.supportsNullUnique)
      if (useUniqueConstraint) {
        if (grammar.supportsUnique) {
          buf.append(" unique")
        } else {
          val uk: UniqueKey = getOrCreateUniqueKey(col.name + '_')
          uk.addColumn(col)
        }
      }

      if (col.hasCheckConstraint && grammar.supportsColumnCheck) {
        buf.append(" check (").append(col.checkConstraint).append(")")
      }
      var columnComment = col.comment
      if (columnComment != null) buf.append(grammar.getColumnComment(columnComment))

      if (iter.hasNext) buf.append(", ")

    }
    if (hasPrimaryKey && primaryKey.enabled) {
      buf.append(", ").append(primaryKey.sqlConstraintString)
    }
    buf.append(')')
    if (null != comment && !comment.isEmpty()) buf.append(grammar.getComment(comment))

    buf.toString()
  }

  def querySql: String = {
    val sb: StringBuilder = new StringBuilder()
    sb.append("select ")
    for (columnName <- this.columnNames) {
      sb.append(columnName).append(',')
    }
    sb.deleteCharAt(sb.length() - 1)
    sb.append(" from ").append(identifier(schema))
    sb.toString()
  }

  override def clone(): Table = {
    val tb: Table = new Table(schema, name)
    tb.comment = comment
    for (col <- columns)
      tb.addColumn(col.clone())
    if (null != primaryKey) {
      tb.primaryKey = primaryKey.clone()
      tb.primaryKey.table = tb
    }

    for (fk <- foreignKeys)
      tb.addForeignKey(fk.clone())

    for (uk <- uniqueKeys)
      tb.addUniqueKey(uk.clone())

    for (idx <- indexes)
      tb.addIndex(idx.clone())
    return tb
  }

  def lowerCase() {
    this.schema = schema.toLowerCase()
    this.name = name.toLowerCase()
    for (col <- columns)
      col.lowerCase

    if (null != primaryKey) primaryKey.lowerCase

    for (fk <- foreignKeys)
      fk.lowerCase;

    for (uk <- uniqueKeys)
      uk.lowerCase;

    for (idx <- indexes)
      idx.lowerCase;
  }

  def compareTo(o: Table): Int = this.identifier.compareTo(o.identifier)

  private def hasPrimaryKey = null != primaryKey

  override def toString = Table.qualify(schema, name)

  def getColumn(columnName: String): Column = {
    for (col <- columns)
      if (col.name == columnName) return col;
    return null;
  }

  def getForeignKey(keyName: String): ForeignKey = {
    if (null == keyName) null
    else foreignKeys.find(f => f.name.equals(keyName)).orNull
  }

  def addForeignKey(key: ForeignKey) {
    key.table = this
    foreignKeys += key
  }

  def addUniqueKey(key: UniqueKey) {
    key.table = this
    this.uniqueKeys += key
  }

  def addColumn(column: Column): Boolean = {
    if (!columns.exists(_.name == column.name)) {
      columns += column.clone();
      true
    } else false
  }

  def addIndex(index: Index) {
    index.table = this
    indexes += index
  }

  def getIndexes = indexes

  def getIndex(indexName: String) = {
    if (null == indexName) null
    else indexes.find(f => f.name.equals(indexName)).orNull
  }
}

object Table {
  def qualify(schema: String, name: String): String = {
    val qualifiedName: StringBuilder = new StringBuilder()
    if (null != schema)
      qualifiedName.append(schema).append('.')

    return qualifiedName.append(name).toString()
  }
}
