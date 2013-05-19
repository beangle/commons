/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import org.beangle.commons.jdbc.dialect._

class Sequence(var name: String) extends Comparable[Sequence] {

  var current: Long = 0

  var increment: Int = 1

  var cache: Int = 32

  def createSql(dialect: Dialect): String = {
    if (null == dialect.sequenceGrammar) return null
    var sql: String = dialect.sequenceGrammar.createSql;
    sql = sql.replace(":name", name)
    sql = sql.replace(":start", String.valueOf(current + 1))
    sql = sql.replace(":increment", String.valueOf(increment))
    sql = sql.replace(":cache", String.valueOf(cache))
    return sql
  }

  def dropSql(dialect: Dialect): String = {
    if (null == dialect.sequenceGrammar) return null
    var sql: String = dialect.sequenceGrammar.dropSql;
    sql = sql.replace(":name", name)
    return sql
  }

  override def toString = name

  override def compareTo(o: Sequence) = name.compareTo(o.name)

  override def hashCode = name.hashCode()

  /**
   * 比较name
   */
  override def equals(rhs: Any) = name.equals(rhs.asInstanceOf[Sequence].name)
}
