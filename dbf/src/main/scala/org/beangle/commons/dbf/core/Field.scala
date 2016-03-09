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

import java.io.{ DataInput, IOException }

import org.beangle.commons.dbf.DbfException
import org.beangle.commons.dbf.core.DataType.DataType

class Field(val index: Int,
    val name: String,
    val dataType: DataType,
    val fieldLength: Int,
    val decimalCount: Byte,
    val workAreaId: Byte,
    val setFieldsFlag: Byte,
    val indexFieldFlag: Byte) {
}

object Field {
  val HEADER_TERMINATOR = 0x0d;

  def read(in: DataInput, index: Int): Field = {
    try {
      //|fileName{0-10}|data-type{11}|reserved{12-15}|field-ength{16}|decimal-count{17}
      //|reserved{18-19}|work-areaid{20}|reserved{21-22}|setFieldsFlag{23}|reserved{24-30}
      //|indexFieldFlag{31}

      // we get end of the dbf header
      val firstByte = in.readByte()
      if (firstByte == HEADER_TERMINATOR) return null;

      val nameBuf = new Array[Byte](11)
      in.readFully(nameBuf, 1, 10)
      nameBuf(0) = firstByte

      var nonZeroIndex = nameBuf.length - 1
      while (nonZeroIndex >= 0 && nameBuf(nonZeroIndex) == 0) nonZeroIndex -= 1

      val name = new String(nameBuf, 0, nonZeroIndex + 1)
      val fieldType = in.readByte();
      val dataType = DataType.valueOf(fieldType)
      if (dataType == null) {
        throw new DbfException(
          String.format("Unsupported Dbf field type: %s",
            Integer.toString(fieldType, 16)))
      }
      in.skipBytes(4)
      val fieldLength = in.readUnsignedByte()
      val decimalCount = in.readByte()
      in.skipBytes(2)
      val workAreaId = in.readByte()
      in.skipBytes(2)
      val setFieldsFlag = in.readByte()
      in.skipBytes(7)
      val indexFieldFlag = in.readByte()
      new Field(index, name, dataType, fieldLength, decimalCount, workAreaId, setFieldsFlag, indexFieldFlag)
    } catch {
      case e: IOException => throw new DbfException("Cannot read Dbf field", e);
    }
  }
}
