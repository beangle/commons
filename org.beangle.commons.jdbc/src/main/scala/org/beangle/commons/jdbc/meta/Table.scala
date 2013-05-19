/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.meta

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.beangle.commons.jdbc.dialect.Dialect
/**
 * JDBC table metadata
 *
 * @author chaostone
 */
class Table(var name: String) extends Comparable[Table] with Cloneable {
  var schema: String = null
  protected[meta] var pk: PrimaryKey = null
  protected[meta] var comment: String = null
  protected[meta] val cols = new ListBuffer[Column]
  protected[meta] val uniqueKeys = new ListBuffer[UniqueKey]
  protected[meta] val foreignKeys = new ListBuffer[ForeignKey]
  protected[meta] val indexes = new ListBuffer[Index]

  def this(schema: String, name: String) {
    this(name)
    this.schema = schema
  }

  def columnNames: List[String] = cols.result.map(_.name)

  def identifier = Table.qualify(schema, name)

  def identifier(givenSchema: String) = {
    if (null == givenSchema || givenSchema.isEmpty()) name
    else Table.qualify(givenSchema, name)
  }

  def getOrCreateUniqueKey(keyName: String) = {
    var uk: UniqueKey = uniqueKeys.find(f => f.name.equals(keyName)).orNull
    if (uk == null) {
      uk = new UniqueKey(keyName)
      uk.table = this
      uniqueKeys += uk
    }
    uk
  }

  def insertSql: String = insertSql(schema)

  def insertSql(newSchema: String): String = {
    val sb = new StringBuilder("insert into ")
    sb ++= identifier(newSchema)
    sb += '('
    sb ++= columnNames.mkString(",")
    sb ++= ") values("
    sb ++= ("?," * cols.size)
    sb.setCharAt(sb.length() - 1, ')')
    sb.mkString
  }

  def createSql(dialect: Dialect): String = createSql(dialect, schema)

  /**
   * @param dialect
   * @return
   */
  def createSql(dialect: Dialect, newSchema: String): String = {
    val grammar = dialect.tableGrammar
    val buf = new StringBuilder(grammar.createString).append(' ').append(identifier(newSchema)).append(" (")
    val iter: Iterator[Column] = cols.iterator
    val l = cols.toList
    while (iter.hasNext) {
      val col: Column = iter.next()
      buf.append(col.name).append(' ')
      buf.append(col.getSqlType(dialect))

      val defaultValue: String = col.defaultValue
      if (defaultValue != null) buf.append(" default ").append(defaultValue)

      if (col.nullable) {
        buf.append(grammar.nullColumnString)
      } else {
        buf.append(" not null")
      }
      val useUniqueConstraint = col.unique && (!col.nullable || grammar.supportsNullUnique)
      if (useUniqueConstraint) {
        if (grammar.supportsUnique) {
          buf.append(" unique")
        } else {
          val uk: UniqueKey = getOrCreateUniqueKey(col.name + '_')
          uk.addColumn(col)
        }
      }

      if (col.hasCheckConstraint && grammar.supportsColumnCheck) {
        buf.append(" check (").append(col.checkConstraint).append(")")
      }
      val columnComment = col.comment
      if (columnComment != null) buf.append(grammar.getColumnComment(columnComment))

      if (iter.hasNext) buf.append(", ")

    }
    if (hasPrimaryKey && pk.enabled) {
      buf.append(", ").append(pk sqlConstraintString)
    }
    buf.append(')')
    if (null != comment && !comment.isEmpty()) {
      buf.append(grammar.getComment(comment))
    }
    buf.toString()
  }

  def querySql: String = querySql(schema)

  def querySql(newSchema: String): String = {
    val sb: StringBuilder = new StringBuilder()
    sb.append("select ")
    for (columnName <- this.columnNames) {
      sb.append(columnName).append(',')
    }
    sb.deleteCharAt(sb.length() - 1)
    sb.append(" from ").append(identifier(newSchema))
    sb.toString()
  }

  override def clone(): Table = {
    val tb: Table = new Table(schema, name)
    tb.setComment(comment)
    for (col <- cols)
      tb.addColumn(col.clone())
    if (null != pk) tb.primaryKey = pk.clone()

    for (fk <- foreignKeys)
      tb.addForeignKey(fk.clone())

    for (uk <- uniqueKeys)
      tb.addUniqueKey(uk.clone())

    for (idx <- indexes)
      tb.addIndex(idx.clone())
    return tb
  }

  def lowerCase() {
    this.schema = schema.toLowerCase()
    this.name = name.toLowerCase()
    for (col <- cols)
      col.lowerCase

    if (null != pk) pk.lowerCase

    for (fk <- foreignKeys)
      fk lowerCase;

    for (uk <- uniqueKeys)
      uk lowerCase;

    for (idx <- indexes)
      idx lowerCase;
  }

  def compareTo(o: Table): Int = this.identifier.compareTo(o.identifier)

  private def hasPrimaryKey = null != pk

  def columns = cols

  def primaryKey = pk

  def primaryKey_=(primaryKey: PrimaryKey) {
    this.pk = primaryKey
    if (null != primaryKey) primaryKey.table = this
  }

  override def toString = Table.qualify(schema, name)

  def getColumn(columnName: String): Column = {
    for (col <- cols)
      if (col.name == columnName) return col;
    return null;
  }

  def getForeignKey(keyName: String): ForeignKey = {
    if (null == keyName) null
    else foreignKeys.find(f => f.name.equals(keyName)).orNull
  }

  def addForeignKey(key: ForeignKey) = {
    key.table = this
    foreignKeys += key
  }

  def addUniqueKey(key: UniqueKey) = {
    key.table = this
    this.uniqueKeys += key
  }

  def addColumn(column: Column): Boolean = {
    if (!cols.exists(_.name == column.name)) {
      cols += column.clone();
      true
    } else false
  }

  def addIndex(index: Index) = indexes += index

  def getForeignKeys = foreignKeys

  def getIndexes = indexes

  def getIndex(indexName: String) = {
    if (null == indexName) null
    else indexes.find(f => f.name.equals(indexName)).orNull
  }

  def getComment = comment

  def setComment(comment: String) = this.comment = comment

}

object Table {
  def qualify(schema: String, name: String): String = {
    val qualifiedName: StringBuilder = new StringBuilder()
    if (null != schema)
      qualifiedName.append(schema).append('.')

    return qualifiedName.append(name).toString()
  }
}
