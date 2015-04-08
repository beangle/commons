/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import org.beangle.commons.codec.Decoder
import org.beangle.commons.codec.Encoder
import javax.crypto.spec.DESKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.Cipher
import java.security.SecureRandom

/**
 * Des ecode and decode utility
 */
object Des {
  def encode(key: String, data: Array[Byte]): Array[Byte] = new DesEncoder(key.getBytes()).encode(data)
  def decode(key: String, data: Array[Byte]): Array[Byte] = new DesDecoder(key.getBytes()).decode(data)
}

class DesEncoder(val key: Array[Byte]) extends Encoder[Array[Byte], Array[Byte]] {
  val deskey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key))

  def encode(data: Array[Byte]): Array[Byte] = {
    val cipher = Cipher.getInstance("DES")
    cipher.init(Cipher.ENCRYPT_MODE, deskey, new SecureRandom())
    cipher.doFinal(data)
  }
}

class DesDecoder(key: Array[Byte]) extends Decoder[Array[Byte], Array[Byte]] {
  val deskey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key))

  def decode(data: Array[Byte]): Array[Byte] = {
    val cipher = Cipher.getInstance("DES")
    cipher.init(Cipher.DECRYPT_MODE, deskey, new SecureRandom())
    cipher.doFinal(data)
  }

}