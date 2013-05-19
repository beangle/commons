/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

class PrimaryKey(name: String, column: Column) extends Constraint(name) {

  override def clone: this.type = return super.clone()

  addColumn(column)

  override def addColumn(column: Column) {
    if (column != null) cols += column
    if (column.nullable) enabled = false;
  }

  def sqlConstraintString = {
    val buf = new StringBuilder("primary key (")
    columns.foreach(col => (buf.append(col.name).append(", ")))
    if (!columns.isEmpty) buf.delete(buf.size - 2, buf.size);
    buf.append(')').result
  }
}
