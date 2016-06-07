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
package org.beangle.commons.dbf

import java.io.{ BufferedInputStream, BufferedWriter, Closeable, DataInput, DataInputStream, EOFException, File, FileInputStream, FileWriter, IOException, InputStream, PrintWriter, RandomAccessFile }
import java.nio.charset.Charset
import java.util.{ Date, GregorianCalendar }

import org.beangle.commons.dbf.core.{ DataType, Field, Header }
import org.beangle.commons.dbf.util.DbfUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Strings.rightPad

import Reader.{ DATA_DELETED, DATA_ENDED }

object Reader {
  private final val DATA_ENDED: Byte = 0x1A
  private final val DATA_DELETED: Byte = 0x2A

  def apply(file: File): Reader = {
    val dataInput = new RandomAccessFile(file, "r")
    new Reader(dataInput, Header.read(dataInput))
  }

  def apply(in: InputStream): Reader = {
    val dataInput = new DataInputStream(new BufferedInputStream(in))
    new Reader(dataInput, Header.read(dataInput))
  }

  def writeToCsv(dbf: File, csv: File, dbfEncoding: Charset) {
    val reader = Reader(dbf)
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(csv)))

    try {
      val header = reader.header
      val titles = new Array[String](header.fields.length)
      val fieldCount = header.fields.length
      (0 until fieldCount) foreach { i =>
        val field = header.fields(i)
        writer.print(field.name.trim())
        if (i + 1 < fieldCount) writer.print(',')
      }
      writer.println()

      var row = reader.nextRecord()
      while (row != null) {
        (0 until fieldCount) foreach { i =>
          val field = header.fields(i)
          val value =
            if (field.dataType == DataType.Char)
              "\"" + new String(row(i).asInstanceOf[Array[Byte]], dbfEncoding).trim() + "\""
            else String.valueOf(row(i))
          writer.print(value)
          if (i + 1 < fieldCount) writer.print(',')
        }
        row = reader.nextRecord()
        writer.println()
      }
    } catch {
      case e: IOException =>
        throw new DbfException("Cannot write .dbf file to .txt", e)
    } finally {
      reader.close()
      writer.close()
    }
  }

  /**
   * Create string with dbf information:
   *   - creation date
   *   - total records count
   *   - columns info
   * @param dbf  .dbf file
   * @return  string with dbf information
   */
  def readInfo(dbf: File): String = {
    val indexWidth = 4
    val nameWidth = 16
    val typeWidth = 8
    val lengthWidth = 8
    val decimalWidth = 8

    var in = new DataInputStream(new BufferedInputStream(new FileInputStream(dbf)))
    try {
      var header = Header.read(in)
      var sb = new StringBuilder(512)
      sb.append("Created at: ")
        .append(header.year).append('-').append(header.month)
        .append('-').append(header.day).append('\n')
        .append("Total records: ").append(header.numberOfRecords).append('\n')
        .append("Header size: ").append(header.headerSize).append('\n')
        .append("Columns: ").append('\n')

      sb.append("  ").append(rightPad("#", indexWidth, ' '))
        .append(rightPad("Name", nameWidth, ' '))
        .append(rightPad("Type", typeWidth, ' '))
        .append(rightPad("Length", lengthWidth, ' '))
        .append(rightPad("Decimal", decimalWidth, ' '))
        .append('\n')

      val totalWidth = indexWidth + nameWidth + typeWidth + lengthWidth + decimalWidth + 2
      sb.append("-" * totalWidth)

      (0 until header.fields.size) foreach { i =>
        val field = header.fields(i)
        sb.append('\n')
          .append("  ").append(rightPad(String.valueOf(i), indexWidth, ' '))
          .append(rightPad(field.name, nameWidth, ' '))
          .append(rightPad(field.dataType.toString, typeWidth, ' '))
          .append(rightPad(String.valueOf(field.fieldLength), lengthWidth, ' '))
          .append(rightPad(String.valueOf(field.decimalCount), decimalWidth, ' '))
      }

      return sb.toString()
    } catch {
      case e: IOException =>
        throw new DbfException("Cannot read header of .dbf file " + dbf, e)
    } finally {
      in.close()
    }
  }

}

/**
 *  @see <a href="http://www.fship.com/dbfspecs.txt">DBF specification</a>
 */
class Reader(dataInput: DataInput, val header: Header) extends Closeable {
  skipToDataBeginning()

  private def skipToDataBeginning(): Unit = {
    // it might be required to jump to the start of records at times
    val dataStartIndex = header.headerSize - 32 * (header.fieldsCount + 1) - 1
    if (dataStartIndex > 0) {
      dataInput.skipBytes(dataStartIndex)
    }
  }

  def canSeek: Boolean = {
    dataInput.isInstanceOf[RandomAccessFile]
  }

  /**
   * Attempt to seek to a specified record index. If successful the record can be read
   * by calling {@link DbfReader#nextRecord()}.
   *
   * @param n The zero-based record index.
   */
  def seekToRecord(n: Int): Unit = {
    if (!canSeek) {
      throw new DbfException("Seeking is not supported.")
    }
    if (n < 0 || n >= header.numberOfRecords) {
      throw new DbfException(Strings.format("Record index out of range [0, %d]: %d",
        header.numberOfRecords, n))
    }
    val position = header.headerSize + n * header.recordSize
    try {
      dataInput.asInstanceOf[RandomAccessFile].seek(position)
    } catch {
      case e: IOException =>
        throw new DbfException(
          Strings.format("Failed to seek to record %d of %d", n, header.numberOfRecords), e)
    }
  }

  /**
   * Reads and returns the next row in the Dbf stream
   *
   * @return The next row as an Object array.
   */
  def nextRecord(): Array[Object] = {
    try {
      var nextByte: Int = 0
      do {
        nextByte = dataInput.readByte()
        if (nextByte == DATA_ENDED) {
          return null
        } else if (nextByte == DATA_DELETED) {
          dataInput.skipBytes(header.recordSize - 1)
        }
      } while (nextByte == DATA_DELETED)

      val recordObjects = new Array[Object](header.fieldsCount)
      (0 until header.fieldsCount) foreach { i =>
        recordObjects(i) = readFieldValue(header.fields(i))
      }
      return recordObjects
    } catch {
      case e: EOFException =>
        return null; // we currently end reading file
      case e: IOException =>
        throw new DbfException("Cannot read next record form Dbf file", e)
    }
  }

  private def readFieldValue(field: Field): Object = {
    val buf = new Array[Byte](field.fieldLength)
    dataInput.readFully(buf)

    field.dataType match {
      case DataType.Char    => readCharacterValue(field, buf)
      case DataType.Date    => readDateValue(field, buf)
      case DataType.Float   => readFloatValue(field, buf)
      case DataType.Logical => readLogicalValue(field, buf)
      case DataType.Numeric => readNumericValue(field, buf)
      case _                => null
    }
  }
  protected def readCharacterValue(field: Field, buf: Array[Byte]): Object = {
    return buf
  }

  protected def readDateValue(field: Field, buf: Array[Byte]): Date = {
    val year = DbfUtils.parseInt(buf, 0, 4)
    val month = DbfUtils.parseInt(buf, 4, 6)
    val day = DbfUtils.parseInt(buf, 6, 8)
    return new GregorianCalendar(year, month - 1, day).getTime()
  }

  protected def readFloatValue(field: Field, buf: Array[Byte]): java.lang.Float = {
    try {
      val floatBuf = DbfUtils.trimLeftSpaces(buf)
      val processable = (floatBuf.length > 0 && !DbfUtils.contains(floatBuf, '?'.asInstanceOf[Byte]))
      if (processable) java.lang.Float.valueOf(new String(floatBuf)) else null
    } catch {
      case e: NumberFormatException =>
        throw new DbfException("Failed to parse Float from " + field.name, e)
    }
  }

  protected def readLogicalValue(field: Field, buf: Array[Byte]): java.lang.Boolean = {
    val isTrue = (buf(0) == 'Y' || buf(0) == 'y' || buf(0) == 'T' || buf(0) == 't')
    if (isTrue) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE
  }

  protected def readNumericValue(field: Field, buf: Array[Byte]): Number = {
    try {
      val numericBuf = DbfUtils.trimLeftSpaces(buf)
      val processable = numericBuf.length > 0 && !DbfUtils.contains(numericBuf, '?'.asInstanceOf[Byte])
      if (processable) java.lang.Double.valueOf(new String(numericBuf)) else null
    } catch {
      case e: NumberFormatException =>
        throw new DbfException("Failed to parse Number from " + field.name, e)
    }
  }

  override def close(): Unit = {
    dataInput match {
      case c: Closeable => c.close()
      case _            =>
    }
  }
}
