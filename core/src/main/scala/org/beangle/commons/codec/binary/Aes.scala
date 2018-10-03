/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.codec.binary

import org.beangle.commons.codec.{ Decoder, Encoder }

import javax.crypto.{ Cipher, SecretKey, SecretKeyFactory }
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object Aes {
  def buildKey(key: String): SecretKey = {
    new SecretKeySpec(key.getBytes("UTF-8"), "AES")
  }

  /**
   * ECB ecode and decode utility
   */
  object ECB {
    def encode2Hex(key: String, data: String): String = {
      val d = new ECBEncoder(key).encode(data.getBytes("UTF-8"))
      Hex.encode(d, true)
    }

    def decodeHex(key: String, data: String): String = {
      new String(new ECBDecoder(key).decode(Hex.decode(data)))
    }

    def encode(key: String, data: Array[Byte]): Array[Byte] = {
      new ECBEncoder(key).encode(data)
    }

    def decode(key: String, data: Array[Byte]): Array[Byte] = {
      new ECBDecoder(key).decode(data)
    }

    def buildCipher(mode: Int, sk: SecretKey): Cipher = {
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      cipher.init(mode, sk)
      cipher
    }
  }
  class ECBEncoder(val key: String) extends Encoder[Array[Byte], Array[Byte]] {
    val skey = Aes.buildKey(key)

    def encode(data: Array[Byte]): Array[Byte] = {
      ECB.buildCipher(Cipher.ENCRYPT_MODE, skey).doFinal(data)
    }
  }

  class ECBDecoder(key: String) extends Decoder[Array[Byte], Array[Byte]] {
    val skey = Aes.buildKey(key)

    def decode(data: Array[Byte]): Array[Byte] = {
      ECB.buildCipher(Cipher.DECRYPT_MODE, skey).doFinal(data)
    }

  }
}
