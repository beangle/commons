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

import javax.crypto.spec.{DESKeySpec, IvParameterSpec}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}

/** @see https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#impl
  */
object Des {
  def buildKey(key: String): SecretKey =
    SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8")))

  /** Cipher-Block Chaining ecode and decode utility
    */
  object CBC {
    def encode2Hex(key: String, data: String, padding: String = Padding.PKCS5, iv: String = null): String = {
      val d = new CBCEncoder(key, iv, padding).encode(data.getBytes("UTF-8"))
      Hex.encode(d, true)
    }

    def decodeHex(key: String, data: String, padding: String = Padding.PKCS5, iv: String = null): String =
      new String(new CBCDecoder(key, iv, padding).decode(Hex.decode(data)))

    def encode(key: String, data: Array[Byte], padding: String = Padding.PKCS5, iv: String = null): Array[Byte] =
      new CBCEncoder(key, iv, padding).encode(data)

    def decode(key: String, data: Array[Byte], padding: String = Padding.PKCS5, iv: String = null): Array[Byte] =
      new CBCDecoder(key, iv, padding).decode(data)

    def buildCipher(mode: Int, key: String, initVector: String, padding: String): Cipher = {
      val cipher = Cipher.getInstance("DES/CBC/" + padding)
      val iv = if (initVector eq null) key else initVector
      val ivs = new IvParameterSpec(iv.getBytes("UTF-8"))
      cipher.init(mode, Des.buildKey(key), ivs)
      cipher
    }
  }

  class CBCEncoder(key: String, iv: String, padding: String) extends Encoder[Array[Byte], Array[Byte]] {
    def encode(data: Array[Byte]): Array[Byte] =
      CBC.buildCipher(Cipher.ENCRYPT_MODE, key, iv, padding).doFinal(data)
  }

  class CBCDecoder(key: String, iv: String, padding: String) extends Decoder[Array[Byte], Array[Byte]] {
    def decode(data: Array[Byte]): Array[Byte] =
      CBC.buildCipher(Cipher.DECRYPT_MODE, key, iv, padding).doFinal(data)
  }
}
