/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
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
