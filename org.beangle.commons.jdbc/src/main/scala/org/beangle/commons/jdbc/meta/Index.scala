/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
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
