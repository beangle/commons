/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
    check != null && check.isDefined
  }

  def literalName(engine: Engine): String = {
    name.toLiteral(engine)
  }

  override def toString: String = {
    name + " " + sqlType.toString
  }
}
