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
package org.beangle.commons.jdbc

import scala.collection.mutable.ListBuffer
import org.beangle.commons.collection.Collections
/**
 * JDBC index metadata
 *
 * @author chaostone
 */
class Index(var table: Table, var name: Identifier) extends Cloneable {

  var columns = Collections.newBuffer[Identifier]

  var unique: Boolean = false

  var ascOrDesc: Option[Boolean] = None

  def toCase(lower: Boolean): Unit = {
    this.name = name.toCase(lower)
    val lowers = columns.map { col => col.toCase(lower) }
    columns.clear()
    columns ++= lowers
  }

  def attach(engine: Engine): Unit = {
    name = name.attach(engine)
    val changed = columns.map { col => col.attach(engine) }
    columns.clear()
    columns ++= changed
  }

  def addColumn(column: Identifier): Unit = {
    if (column != null) columns += column
  }

  override def toString: String = {
    "Index(" + literalName + ')'
  }

  override def clone(): this.type = {
    val cloned = super.clone().asInstanceOf[this.type]
    val newColumns = Collections.newBuffer[Identifier]
    newColumns ++= columns
    cloned.columns = newColumns
    cloned
  }

  def literalName: String = {
    name.toLiteral(table.schema.database.engine)
  }

}
