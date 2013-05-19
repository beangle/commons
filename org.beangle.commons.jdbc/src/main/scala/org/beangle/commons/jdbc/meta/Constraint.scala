/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import collection.mutable.ListBuffer

/**
 * Table Constraint Metadata
 *
 * @author chaostone
 */
class Constraint(var name: String) extends Comparable[Constraint] with Cloneable {

  protected[meta] val cols = new ListBuffer[Column];

  var enabled: Boolean = true;

  var table: Table = null

  def lowerCase() { if (null != name) this.name = name.toLowerCase }

  def addColumn(column: Column) { if (column != null) cols += column }

  def columns: List[Column] = cols.toList

  override def compareTo(o: Constraint) = { if (null == name) 0 else name.compareTo(o.name) }

  override def clone(): this.type = {
    var cloned: this.type = null
    cloned = super.clone().asInstanceOf[this.type]
    var newColumns = new ListBuffer[Column];
    for (column <- cols) {
      newColumns += column.clone();
    }
    cloned.cols.clear();
    cloned.cols ++= newColumns
    return cloned;
  }

}
