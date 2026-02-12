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

import java.io.DataInput

/** Low-level DBF I/O utilities. */
object DbfUtils {
  /** Reads a 32-bit little-endian integer. */
  def readLittleEndianInt(in: DataInput): Int = {
    var bigEndian = 0
    Range(0, 32, 8) foreach { shiftBy =>
      bigEndian |= (in.readUnsignedByte() & 0xff) << shiftBy
    }
    bigEndian
  }

  /** Reads a 16-bit little-endian short. */
  def readLittleEndianShort(in: DataInput): Short = {
    val low = in.readUnsignedByte() & 0xff
    val high = in.readUnsignedByte()
    (high << 8 | low).asInstanceOf[Short]
  }

  /** Trims trailing spaces from byte array. */
  def trimLeftSpaces(arr: Array[Byte]): Array[Byte] = {
    var i = arr.length - 1
    while (i >= 0 && arr(i) == ' ') i -= 1
    i += 1
    val result = new Array[Byte](i)
    if (i > 0) System.arraycopy(arr, 0, result, 0, i)
    result
  }

  /** Returns true if the byte array contains the given value. */
  def contains(arr: Array[Byte], value: Byte): Boolean =
    arr.contains(value)

  /** Parses a positive integer from bytes. */
  def parseInt(bytes: Array[Byte]): Int = {
    var result = 0
    var i = 0
    while (i < bytes.length) {
      val aByte = bytes(i)
      if (aByte == ' ') return result
      result *= 10
      result += (aByte - '0'.asInstanceOf[Byte])
      i += 1
    }
    result
  }

  /** Parses a positive integer from a byte range. */
  def parseInt(bytes: Array[Byte], from: Int, to: Int): Int = {
    var result = 0
    var i = from
    while (i < to && i < bytes.length) {
      result *= 10
      result += (bytes(i) - '0'.asInstanceOf[Byte])
      i += 1
    }
    result
  }

  /** Parses a positive long from bytes.
   *
   * @param bytes the byte array
   * @return the parsed value
   */
  def parseLong(bytes: Array[Byte]): Long = {
    var result = 0L
    var i = 0
    while (i < bytes.length) {
      val aByte = bytes(i)
      if (aByte == ' ') return result
      result *= 10
      result += (aByte - '0'.asInstanceOf[Byte])
      i += 1
    }
    result
  }

  /** Parses a positive long from a byte range. */
  def parseLong(bytes: Array[Byte], from: Int, to: Int): Long = {
    var result = 0L
    var i = 0
    while (i < to && i < bytes.length) {
      result *= 10
      result += (bytes(i) - '0'.asInstanceOf[Byte])
      i += 1
    }
    result
  }
}
