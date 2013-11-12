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

import collection.mutable.ListBuffer

/**
 * Table Constraint Metadata
 *
 * @author chaostone
 */
class Constraint(var name: String) extends Comparable[Constraint] with Cloneable {

  val columns = new ListBuffer[Column]

  var enabled: Boolean = true

  var table: Table = null

  def lowerCase() { if (null != name) this.name = name.toLowerCase }

  def addColumn(column: Column) { if (column != null) columns += column }

  override def compareTo(o: Constraint) = { if (null == name) 0 else name.compareTo(o.name) }

  override def clone(): this.type = {
    var cloned: this.type = null
    cloned = super.clone().asInstanceOf[this.type]
    var newColumns = new ListBuffer[Column];
    for (column <- columns) {
      newColumns += column.clone();
    }
    cloned.columns.clear();
    cloned.columns ++= newColumns
    return cloned;
  }

}
