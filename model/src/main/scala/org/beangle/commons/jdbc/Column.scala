package org.beangle.commons.jdbc

/**
 * JDBC column metadata
 *
 * @author chaostone
 */
class Column(var name: Identifier, var sqlType: SqlType, var nullable: Boolean = true) extends Cloneable with Comment {

  var unique: Boolean = false
  var defaultValue: Option[String] = None
  var check: Option[String] = None

  def this(name: String, sqlType: SqlType) {
    this(Identifier(name), sqlType)
  }

  override def clone(): this.type = {
    super.clone().asInstanceOf[this.type]
  }

  def toCase(lower: Boolean): Unit = {
    this.name = name.toCase(lower)
  }

  def hasCheck: Boolean = {
    check != null
  }

  def literalName(engine: Engine): String = {
    name.toLiteral(engine)
  }

  override def toString: String = {
    "Column(" + name + ')'
  }
}
