/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.codec.binary

import java.io.UnsupportedEncodingException
import org.beangle.commons.codec.net.BCoder

object Base64 {

  def encode(data: Array[Byte]): Array[Char] = {
    val out = new Array[Char](((data.length + 2) / 3) * 4)
    var i = 0
    var index = 0
    while (i < data.length) {
      var quad = false
      var trip = false
      var `val` = 0xff & data(i)
      `val` <<= 8
      if (i + 1 < data.length) {
        `val` |= 0xff & data(i + 1)
        trip = true
      }
      `val` <<= 8
      if (i + 2 < data.length) {
        `val` |= 0xff & data(i + 2)
        quad = true
      }
      out(index + 3) = alphabet(if (quad) `val` & 0x3f else 64)
      `val` >>= 6
      out(index + 2) = alphabet(if (trip) `val` & 0x3f else 64)
      `val` >>= 6
      out(index + 1) = alphabet(`val` & 0x3f)
      `val` >>= 6
      out(index + 0) = alphabet(`val` & 0x3f)
      i += 3
      index += 4
    }
    out
  }

  def decode(pArray: String): Array[Byte] = decode(pArray.toCharArray())

  def decode(data: Array[Char]): Array[Byte] = {
    var tempLen = data.length
    for (ix <- 0 until data.length if data(ix) > '\377' || codes(data(ix)) < 0) tempLen -= 1
    var len = (tempLen / 4) * 3
    if (tempLen % 4 == 3) len += 2
    if (tempLen % 4 == 2) len += 1
    val out = new Array[Byte](len)
    var shift = 0
    var accum = 0
    var index = 0
    for (ix <- 0 until data.length) {
      val value = if (data(ix) <= '\377') (codes(data(ix))).toInt else -1
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

  private var alphabet: Array[Char] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
    .toCharArray()

  private var codes = new Array[Byte](256)

  for (i <- 0 until 256) codes(i) = -1
  codes(43) = 62
  codes(47) = 63
  for (i <- 48 to 57) codes(i) = ((52 + i) - 48).toByte
  for (i <- 65 to 90) codes(i) = (i - 65).toByte
  for (i <- 97 to 122) codes(i) = ((26 + i) - 97).toByte

  def main(args: Array[String]) {
    println(new BCoder().encode("汉字123"))
    println(Base64.encode("汉字123".getBytes("UTF-8")))
  }
}
