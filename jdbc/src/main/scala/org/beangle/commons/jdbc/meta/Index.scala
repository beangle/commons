
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
import org.beangle.commons.jdbc.dialect.Dialect
/**
 * JDBC index metadata
 *
 * @author chaostone
 */
class Index(var name: String, var table: Table) extends Cloneable {

  val columns = new ListBuffer[Column];

  var unique: Boolean = false

  var ascOrDesc: Option[Boolean] = None

  def lowerCase() = this.name = name.toLowerCase()

  def addColumn(column: Column) = if (column != null) columns += column

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

  def createSql(dialect: Dialect): String = {
    val buf = new StringBuilder("create")
      .append(if (unique) " unique" else "")
      .append(" index ")
      .append(name)
      .append(" on ")
      .append(table.identifier)
      .append(" (");
    val iter = columns.iterator
    while (iter.hasNext) {
      buf.append(iter.next.name);
      if (iter.hasNext) buf.append(", ");
    }
    buf.append(")");
    buf.toString()
  }

  def dropSql(dialect: Dialect): String = "drop index " + table.identifier + "." + name;

}
