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

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKey}

/** Advanced Encryption Standard
  *
  * @see https://en.wikipedia.org/wiki/Advanced_Encryption_Standard
  */
object Aes {
  def buildKey(key: String): SecretKey =
    new SecretKeySpec(key.getBytes("UTF-8"), "AES")

  /** Electronic Codebook ecode and decode utility
    *
    * @see https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Electronic_Codebook_.28ECB.29
    */
  object ECB {
    def encode2Hex(key: String, data: String, padding: String = Padding.PKCS5): String = {
      val d = new ECBEncoder(key, padding).encode(data.getBytes("UTF-8"))
      Hex.encode(d, true)
    }

    def decodeHex(key: String, data: String, padding: String = Padding.PKCS5): String =
      new String(new ECBDecoder(key, padding).decode(Hex.decode(data)))

    def encode(key: String, data: Array[Byte], padding: String = Padding.PKCS5): Array[Byte] =
      new ECBEncoder(key, padding).encode(data)

    def decode(key: String, data: Array[Byte], padding: String = Padding.PKCS5): Array[Byte] =
      new ECBDecoder(key, padding).decode(data)

    def buildCipher(mode: Int, sk: SecretKey, padding: String): Cipher = {
      val cipher = Cipher.getInstance("AES/ECB/" + padding)
      cipher.init(mode, sk)
      cipher
    }
  }

  class ECBEncoder(val key: String, padding: String) extends Encoder[Array[Byte], Array[Byte]] {
    private val skey = Aes.buildKey(key)

    def encode(data: Array[Byte]): Array[Byte] =
      ECB.buildCipher(Cipher.ENCRYPT_MODE, skey, padding).doFinal(data)
  }

  class ECBDecoder(key: String, padding: String) extends Decoder[Array[Byte], Array[Byte]] {
    private val skey = Aes.buildKey(key)

    def decode(data: Array[Byte]): Array[Byte] =
      ECB.buildCipher(Cipher.DECRYPT_MODE, skey, padding).doFinal(data)
  }

  /** Cipher-Block Chaining encode and decode utility
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
      val sk = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
      val iv = if (initVector eq null) key else initVector
      val ivs = new IvParameterSpec(iv.getBytes("UTF-8"))
      val cipher = Cipher.getInstance("AES/CBC/" + padding)
      cipher.init(mode, sk, ivs)
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
