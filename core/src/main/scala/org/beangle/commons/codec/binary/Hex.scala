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

import org.beangle.commons.codec._
/**
 * Hex encode and decode method
 */
object Hex {
  val LowerDigits = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
  val UpperDigits = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  def encode(data: Array[Byte], toLowerCase: Boolean = true): String = HexEncoder.encodeHex(data, toLowerCase)

  def decode(data: String): Array[Byte] = HexDecoder.decode(data)
}

/**
 * Hex decoder
 */
object HexEncoder extends Encoder[Array[Byte], String] {
  import Hex._

  /**
   * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order
   *
   * @param data   a byte[] to convert to Hex characters
   * @param toLowerCase should to lower case
   * @return A Array[Char] containing hexadecimal characters
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

  def encode(data: Array[Byte]): String = encodeHex(data, true)
}

/**
 * Hex Decoder
 */
object HexDecoder extends Decoder[String, Array[Byte]] {

  /**
   * Converts a hexadecimal character to an integer.
   */
  private def toDigit(ch: Char, index: Int): Int = {
    val digit = Character.digit(ch, 16)
    if (digit == -1) throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index)
    digit
  }

  /**
   * Converts an array of character bytes representing hexadecimal values into an array of bytes of those same values.
   * The returned array will be half the length of the passed array, as it takes two characters to represent any given
   * byte. An exception is thrown if the passed char array has an odd number of elements.
   */
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
