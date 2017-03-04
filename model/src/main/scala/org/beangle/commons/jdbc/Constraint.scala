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
 * Table Constraint Metadata
 *
 * @author chaostone
 */
class Constraint(var table: Table, var name: Identifier) extends Ordered[Constraint] with Cloneable {

  var columns = new ListBuffer[Identifier]

  var enabled: Boolean = true

  def literalName: String = {
    name.toLiteral(table.schema.database.engine)
  }

  def attach(engine: Engine): Unit = {
    name = name.attach(engine)
    val changed = columns.map { col => col.attach(engine) }
    columns.clear()
    columns ++= changed
  }

  def toCase(lower: Boolean): Unit = {
    if (null != name) this.name = name.toCase(lower)
    val lowers = columns.map { col => col.toCase(lower) }
    columns.clear()
    columns ++= lowers
  }

  def columnNames: List[String] = {
    columns.map(x => x.toLiteral(table.schema.database.engine)).toList
  }

  def addColumn(column: Identifier): Unit = {
    if (column != null) columns += column
  }

  override def compare(o: Constraint): Int = {
    if (null == name) 0 else name.compare(o.name)
  }

  override def clone(): this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    var newColumns = new ListBuffer[Identifier]
    newColumns ++= columns
    cloned.columns = newColumns
    cloned
  }

}
