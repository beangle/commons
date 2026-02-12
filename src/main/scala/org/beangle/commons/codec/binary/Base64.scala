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

import org.beangle.commons.codec.{Decoder, Encoder}

import java.io.{File, FileOutputStream, IOException}

/** Base64 encode/decode and file dump. */
object Base64 {

  /** Encodes bytes to Base64 string.
   *
   * @param data the bytes to encode
   * @return Base64 string
   */
  def encode(data: Array[Byte]): String = Base64Encoder.encode(data)

  /** Decodes Base64 string to bytes.
   *
   * @param data the Base64 string
   * @return decoded bytes
   */
  def decode(data: String): Array[Byte] = Base64Decoder.decode(data)

  /** Decodes Base64 string and writes to file.
   *
   * @param data Base64-encoded string
   * @param file target file to write
   */
  def dump(data: String, file: File): Unit = {
    var os: FileOutputStream = null
    try {
      os = new FileOutputStream(file)
      os.write(decode(data))
    } catch {
      case e: IOException => e.printStackTrace()
    } finally if (os != null) try {
      os.flush()
      os.close()
    } catch {
      case e: IOException =>
    }
  }
}

/** Base64 encoder (bytes -> string). */
object Base64Encoder extends Encoder[Array[Byte], String] {

  /** Encodes bytes to Base64 string. */
  def encode(data: Array[Byte]): String = {
    new String(java.util.Base64.getEncoder.encode(data))
  }
}

/** Base64 decoder (string -> bytes). */
object Base64Decoder extends Decoder[String, Array[Byte]] {

  /** Decodes Base64 string to bytes. */
  def decode(str: String): Array[Byte] = {
    java.util.Base64.getDecoder.decode(str)
  }

}
