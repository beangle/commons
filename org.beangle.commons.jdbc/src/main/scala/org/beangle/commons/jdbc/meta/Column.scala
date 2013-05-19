/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import java.sql._
import org.beangle.commons.jdbc.dialect.Dialect
import java.util.StringTokenizer

/**
 * DBC column metadata
 *
 * @author chaostone
 */
class Column(colName: String, initTypeCode: Int) extends Comparable[Column] with Cloneable {
  var name: String = colName
  var typeName: String = null
  var typeCode: Int = initTypeCode
  // charactor length or numeric precision
  var size: Int = _
  var scale: Short = _
  var nullable: Boolean = _
  var defaultValue: String = null
  var unique: Boolean = _
  var comment: String = null
  var checkConstraint: String = null

  var position: Int = _

  if (initTypeCode == Types.VARCHAR) this.size = 255

  def this(rs: ResultSet) {
    this(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"))
    position = rs.getInt("ORDINAL_POSITION")
    size = rs.getInt("COLUMN_SIZE")
    scale = rs.getShort("DECIMAL_DIGITS")
    nullable = "yes".equalsIgnoreCase(rs.getString("IS_NULLABLE"))
    typeName = new StringTokenizer(rs.getString("TYPE_NAME"), "() ").nextToken()
    comment = rs.getString("REMARKS")
  }

  override def clone = super.clone().asInstanceOf[Column]

  def lowerCase = this.name = name.toLowerCase

  def hasCheckConstraint = checkConstraint != null

  def getSqlType(dialect: Dialect) = dialect.typeNames.get(typeCode, size, size, scale)

  override def toString() = "Column(" + name + ')'

  override def compareTo(other: Column) = position - other.position
}
