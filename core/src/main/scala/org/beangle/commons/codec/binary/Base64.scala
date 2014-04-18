/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.codec.binary

import java.io.UnsupportedEncodingException
import org.beangle.commons.codec.net.BCoder
import org.beangle.commons.codec.Encoder
import org.beangle.commons.codec.Decoder

object Base64 {
  def encode(data: Array[Byte]) = Base64Encoder.encode(data)
  def decode(data: String) = Base64Decoder.decode(data)
  def decode(data: Array[Char]) = Base64Decoder.decode(data)
}

object Base64Encoder extends Encoder[Array[Byte], String] {

  private val Alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray()

  def encode(data: Array[Byte]): String = {
    val out = new Array[Char](((data.length + 2) / 3) * 4)
    var i = 0
    var index = 0
    while (i < data.length) {
      var quad = false
      var trip = false
      var value = 0xff & data(i)
      value <<= 8
      if (i + 1 < data.length) {
        value |= 0xff & data(i + 1)
        trip = true
      }
      value <<= 8
      if (i + 2 < data.length) {
        value |= 0xff & data(i + 2)
        quad = true
      }
      out(index + 3) = Alphabets(if (quad) value & 0x3f else 64)
      value >>= 6
      out(index + 2) = Alphabets(if (trip) value & 0x3f else 64)
      value >>= 6
      out(index + 1) = Alphabets(value & 0x3f)
      value >>= 6
      out(index + 0) = Alphabets(value & 0x3f)
      i += 3
      index += 4
    }
    new String(out)
  }
}

object Base64Decoder extends Decoder[String, Array[Byte]] {

  private val Codes = buildCodes()

  def decode(pArray: String): Array[Byte] = decode(pArray.toCharArray())

  def decode(data: Array[Char]): Array[Byte] = {
    var tempLen = data.length
    for (ix <- 0 until data.length if data(ix) > '\377' || Codes(data(ix)) < 0) tempLen -= 1
    var len = (tempLen / 4) * 3
    if (tempLen % 4 == 3) len += 2
    if (tempLen % 4 == 2) len += 1
    val out = new Array[Byte](len)
    var shift = 0
    var accum = 0
    var index = 0
    (0 until data.length) foreach { ix =>
      val value = if (data(ix) <= '\377') (Codes(data(ix))).toInt else -1
      if (value >= 0) {
        accum <<= 6
        shift += 6
        accum |= value
        if (shift >= 8) {
          shift -= 8
          out(index) = (accum >> shift & 0xff).toByte
          index += 1
        }
      }
    }
    if (index != out.length) throw new Error("Miscalculated data length (wrote " + index + " instead of " +
      out.length +
      ")")
    else out
  }

  private def buildCodes(): Array[Byte] = {
    val codes = new Array[Byte](256)
    (0 until 256) foreach { i => codes(i) = -1 }
    codes(43) = 62
    codes(47) = 63
    (48 to 57) foreach (i => codes(i) = ((52 + i) - 48).toByte)
    (65 to 90) foreach (i => codes(i) = (i - 65).toByte)
    (97 to 122) foreach (i => codes(i) = ((26 + i) - 97).toByte)
    codes
  }
}
