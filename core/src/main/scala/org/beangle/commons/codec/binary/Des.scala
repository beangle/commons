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
import javax.crypto.spec.{ DESKeySpec, IvParameterSpec }
/**
 * @see https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#impl
 */
object Des {
  def buildKey(key: Array[Byte]): SecretKey = {
    SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key))
  }
  /**
   * CBC ecode and decode utility
   */
  object CBC {
    def encode2Hex(key: String, data: String): String = {
      val d = new CBCEncoder(key.getBytes("UTF-8")).encode(data.getBytes("UTF-8"))
      Hex.encode(d, true)
    }
    def decodeHex(key: String, data: String): String = {
      new String(new CBCDecoder(key.getBytes("UTF-8")).decode(Hex.decode(data)))
    }
    def encode(key: String, data: Array[Byte]): Array[Byte] = {
      new CBCEncoder(key.getBytes("UTF-8")).encode(data)
    }
    def decode(key: String, data: Array[Byte]): Array[Byte] = {
      new CBCDecoder(key.getBytes("UTF-8")).decode(data)
    }
    def buildCipher(mode: Int, sk: SecretKey, key: Array[Byte]): Cipher = {
      val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
      cipher.init(mode, sk, new IvParameterSpec(key))
      cipher
    }
  }
  class CBCEncoder(val key: Array[Byte]) extends Encoder[Array[Byte], Array[Byte]] {
    val deskey = Des.buildKey(key)

    def encode(data: Array[Byte]): Array[Byte] = {
      CBC.buildCipher(Cipher.ENCRYPT_MODE, deskey, key).doFinal(data)
    }
  }
  class CBCDecoder(key: Array[Byte]) extends Decoder[Array[Byte], Array[Byte]] {
    val deskey = Des.buildKey(key)

    def decode(data: Array[Byte]): Array[Byte] = {
      CBC.buildCipher(Cipher.DECRYPT_MODE, deskey, key).doFinal(data)
    }
  }
}
