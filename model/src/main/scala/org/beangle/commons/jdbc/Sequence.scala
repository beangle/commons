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

import org.beangle.commons.lang.Strings

class Sequence(var schema: Schema, var name: Identifier) extends Ordered[Sequence] {

  var current: Long = 0

  var increment: Int = 1

  var cache: Int = 32

  var cycle: Boolean = _

  def qualifiedName: String = {
    val engine = schema.database.engine
    schema.name.toLiteral(engine) + "." + name.toLiteral(engine)
  }

  def toCase(lower: Boolean): Unit = {
    this.name = name.toCase(lower)
  }

  def attach(engine: Engine): this.type = {
    this.name = name.attach(engine)
    this
  }

  override def toString: String = {
    name.toString
  }

  override def compare(o: Sequence): Int = {
    name.compareTo(o.name)
  }

  override def hashCode: Int = {
    name.hashCode()
  }

  override def equals(rhs: Any): Boolean = {
    name.value.equals(rhs.asInstanceOf[Sequence].name.value)
  }
}
