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

import scala.collection.mutable.ListBuffer

/**
 * JDBC foreign key metadata
 *
 * @author chaostone
 */
class ForeignKey(t: Table, n: Identifier, column: Identifier = null) extends Constraint(t, n) {

  var cascadeDelete: Boolean = false
  var referencedColumns = new ListBuffer[Identifier]
  var referencedTable: TableRef = _

  addColumn(column)

  override def toCase(lower: Boolean): Unit = {
    super.toCase(lower)
    val lowers = referencedColumns.map { col => col.toCase(lower) }
    referencedColumns.clear()
    referencedColumns ++= lowers

    if (this.table.schema == referencedTable.schema) referencedTable.toCase(lower)
  }

  override def attach(engine: Engine): Unit = {
    super.attach(engine)
    val changed = referencedColumns.map { col => col.attach(engine) }
    referencedColumns.clear()
    referencedColumns ++= changed

    if (this.table.schema == referencedTable.schema) referencedTable.name = referencedTable.name.attach(engine)

  }

  override def clone(): this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    cloned.cascadeDelete = this.cascadeDelete
    cloned.referencedTable = this.referencedTable
    var newColumns = new ListBuffer[Identifier]
    newColumns ++= referencedColumns
    cloned.referencedColumns = newColumns
    cloned
  }

  def refer(table: Table, cols: Identifier*): Unit = {
    this.referencedTable = TableRef(table.schema, table.name)
    if (!cols.isEmpty) referencedColumns ++= cols
  }

  def refer(table: TableRef, cols: Identifier*): Unit = {
    this.referencedTable = table
    if (!cols.isEmpty) referencedColumns ++= cols
  }

  override def toString = "Foreign key(" + name + ')'
}
