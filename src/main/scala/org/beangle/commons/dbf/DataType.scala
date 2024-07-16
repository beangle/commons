/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.dbf

object DataType {
  def valueOf(bv: Byte): DataType = values.find(p => p.v == bv).orNull
}

enum DataType(val v: Byte) {
  def this(c: Char) = {
    this((c & 0xff).asInstanceOf[Byte])
  }

  override def toString: String =
    String.valueOf(v.asInstanceOf[Char])

  case Char extends DataType('C')
  case Date extends DataType('D')
  case Float extends DataType('F')
  case Logical extends DataType('L')
  case Numeric extends DataType('N')
}
