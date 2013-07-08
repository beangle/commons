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
package org.beangle.commons.jdbc.meta

import org.beangle.commons.jdbc.dialect.Dialect
import scala.collection.mutable.ListBuffer

/**
 * JDBC foreign key metadata
 *
 * @author chaostone
 */
class ForeignKey(name: String, column: Column) extends Constraint(name) {

  var referencedTable: Table = null
  var cascadeDelete: Boolean = false
  var referencedColumns = new ListBuffer[Column]

  addColumn(column)

  def this(name: String) = this(name, null)

  def getAlterSql(dialect: Dialect): String = getAlterSql(dialect, table.schema)

  def getAlterSql(dialect: Dialect, newSchema: String) = {
    assert(null != name)
    assert(null != table)
    assert(null != referencedTable, "referencedTable must be set")
    assert(!isReferenceToPrimaryKey || null != referencedTable.primaryKey,
      " reference columns is empty  so the table must has a primary key.")
    assert(!columns.isEmpty, "column's size should greate than 0")

    val cols: Array[String] = new Array[String](columns.size)
    val refcols: Array[String] = new Array[String](columns.size)
    var i: Int = 0
    var refiter: Iterator[Column] = null
    if (isReferenceToPrimaryKey) {
      refiter = referencedTable.primaryKey.columns.iterator
    } else {
      refiter = referencedColumns.iterator
    }

    val iter: Iterator[Column] = columns.iterator
    while (iter.hasNext) {
      cols(i) = iter.next().name
      refcols(i) = refiter.next().name
      i += 1
    }

    val result = "alter table " + table.identifier(newSchema) + dialect.getAddForeignKeyConstraintString(name, cols,
      referencedTable.identifier(newSchema), refcols, isReferenceToPrimaryKey)

    if (cascadeDelete && dialect.supportsCascadeDelete) result + " on delete cascade" else result
  }

  override def clone: this.type = {
    val cloned = super.clone()
    cloned.cascadeDelete = this.cascadeDelete
    cloned.referencedTable = this.referencedTable
    val newColumns = new ListBuffer[Column]
    for (column <- referencedColumns)
      newColumns += column.clone()

    cloned.referencedColumns = newColumns
    return cloned
  }

  def addReferencedColumn(column: Column) = referencedColumns += column

  def getReferencedColumns: List[Column] = referencedColumns.toList

  def isReferenceToPrimaryKey: Boolean = referencedColumns.isEmpty

  def getReferencedTable = referencedTable

  def setReferencedTable(referencedTable: Table) = this.referencedTable = referencedTable

  def isCascadeDelete = cascadeDelete

  def setCascadeDelete(cascadeDelete: Boolean) = this.cascadeDelete = cascadeDelete

  override def toString = "Foreign key(" + name + ')'
}
