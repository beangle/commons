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

package org.beangle.commons.codec.binary

import org.beangle.commons.codec.*

/** Hex encode and decode. */
object Hex {
  /** Hex digits for lower-case output. */
  val LowerDigits = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
  /** Hex digits for upper-case output. */
  val UpperDigits = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  /** Encodes bytes to hex string. */
  def encode(data: Array[Byte], toLowerCase: Boolean = true): String = HexEncoder.encodeHex(data, toLowerCase)

  /** Decodes hex string to bytes. */
  def decode(data: String): Array[Byte] = HexDecoder.decode(data)
}

/** Hex encoder. */
object HexEncoder extends Encoder[Array[Byte], String] {

  import Hex.*

  /** Converts bytes to hex characters.
   *
   * @param data        bytes to convert
   * @param toLowerCase use lower-case hex digits
   * @return hex string
   */
  def encodeHex(data: Array[Byte], toLowerCase: Boolean): String = {
    val digits = if (toLowerCase) LowerDigits else UpperDigits
    val l = data.length
    val out = new Array[Char](l << 1)
    var i, j = 0
    // two characters form the hex value.
    while (i < l) {
      out(j) = digits((0xF0 & data(i)) >>> 4)
      j += 1
      out(j) = digits(0x0F & data(i))
      j += 1
      i += 1
    }
    new String(out)
  }

  /** Encodes bytes to hex string (lowercase). */
  def encode(data: Array[Byte]): String = encodeHex(data, true)
}

/** Hex decoder. */
object HexDecoder extends Decoder[String, Array[Byte]] {

  /** Converts a hex character to an integer. */
  private def toDigit(ch: Char, index: Int): Int = {
    val digit = Character.digit(ch, 16)
    if (digit == -1) throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index)
    digit
  }

  /** Converts hex string to bytes. Two chars per byte; odd-length string throws. */
  def decode(data: String): Array[Byte] = {
    val d = data.toCharArray
    val len = d.length
    if ((len & 0x01) != 0) throw new RuntimeException("Odd number of characters.")
    val out = new Array[Byte](len >> 1)
    // two characters form the hex value.
    var i, j = 0
    while (j < len) {
      var f = toDigit(d(j), j) << 4
      j += 1
      f = f | toDigit(d(j), j)
      j += 1
      out(i) = (f & 0xFF).asInstanceOf[Byte]
      i += 1
    }
    out
  }
}
