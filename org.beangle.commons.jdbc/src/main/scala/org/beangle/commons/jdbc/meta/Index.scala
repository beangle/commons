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

import scala.collection.mutable.ListBuffer

/**
 * JDBC index metadata
 *
 * @author chaostone
 */
class Index(var name: String) extends Cloneable {

  val columns = new ListBuffer[Column];

  def lowerCase = this.name = name.toLowerCase()

  def getName = name

  def addColumn(column: Column) = if (column != null) columns += column

  def getColumns = columns

  override def toString = "IndexMatadata(" + name + ')'

  override def clone: Index = {
    val cloned: Index = super.clone().asInstanceOf[Index]
    val newColumns = new ListBuffer[Column]
    for (column <- columns) {
      newColumns += column.clone()
    }
    cloned.columns.clear()
    cloned.columns ++= newColumns
    return cloned
  }

}
