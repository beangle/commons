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
package org.beangle.commons.dbf.core

import scala.annotation.meta.field
import java.io.IOException
import java.io.DataInput
import org.beangle.commons.dbf.DbfException
import scala.collection.mutable.ListBuffer
import org.beangle.commons.dbf.util.DbfUtils

class Header(
    val signature: Byte,
    val year: Byte,
    val month: Byte,
    val day: Byte,
    val numberOfRecords: Int,
    val headerSize: Short,
    val recordSize: Short,
    val incompleteTransaction: Byte,
    val encryptionFlag: Byte,
    val mdxFlag: Byte,
    val languageDriver: Byte,
    val fields: Array[Field]) {

  def fieldsCount: Int = {
    fields.size
  }

}

object Header {
  def read(dataInput: DataInput): Header = {
    try {
      //|head{0}|yymmdd{1-3}|numberOfRecords{4-7}|headlength{8-9}|recordLength{10-11}|reserved{12-13}|transaction{14}
      //|encryption{15}|multi-user-env{16-27}|mdxFlag{28}|languageDriver{29}|reserved{30-31}|field{n*32}|terminate{0x0D}
      val signature = dataInput.readByte()
      val year = dataInput.readByte()
      val month = dataInput.readByte()
      val day = dataInput.readByte()
      val numberOfRecords = DbfUtils.readLittleEndianInt(dataInput)

      val headerLength = DbfUtils.readLittleEndianShort(dataInput)
      val recordLength = DbfUtils.readLittleEndianShort(dataInput)
      dataInput.skipBytes(2);
      val incompleteTransaction = dataInput.readByte()
      val encryptionFlag = dataInput.readByte()
      dataInput.skipBytes(12);
      val mdxFlag = dataInput.readByte();
      val languageDriver = dataInput.readByte()
      val reserv4 = DbfUtils.readLittleEndianShort(dataInput)

      val fields = new ListBuffer[Field]
      var fieldIndex = 0;
      var field = Field.read(dataInput, fieldIndex)
      while (field != null) { /* 32 each */
        fields += field
        fieldIndex += 1
        field = Field.read(dataInput, fieldIndex)
      }
      new Header(signature, year, month, day, numberOfRecords, headerLength, recordLength, incompleteTransaction, encryptionFlag, mdxFlag, languageDriver, fields.toArray);
    } catch {
      case e: IOException =>
        throw new DbfException("Cannot read Dbf header", e);
    }
  }
}
