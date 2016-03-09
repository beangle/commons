/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.dbf.core

object DataType extends Enumeration {
  class DataType(val v: Byte) extends Val {
    def this(c: Char) {
      this((c & 0xff).asInstanceOf[Byte])
    }
    override def toString = {
      String.valueOf(v.asInstanceOf[Char])
    }
  }

  val Char = new DataType('C')
  val Date = new DataType('D')
  val Float = new DataType('F')
  val Logical = new DataType('L')
  val Numeric = new DataType('N')

  def valueOf(bv: Byte): DataType = {
    values.find(p => p.asInstanceOf[DataType].v == bv).getOrElse(null).asInstanceOf[DataType]
  }
}
