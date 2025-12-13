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

package org.beangle.commons.cdi

import org.beangle.commons.codec.binary.PBEEncryptor
import org.beangle.commons.lang.Strings

object PropertyProcessor {
  def env(): PropertyProcessor = {
    var password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD")
    if (null == password) {
      password = System.getProperty("jasypt.encryptor.password")
    }
    if (Strings.isEmpty(password)) {
      NonePropertyProcessor
    } else {
      PBEPropertyProcessor(PBEEncryptor.random(password))
    }
  }
}

trait PropertyProcessor {
  def encrypt(message: String): String

  def decrypt(input: String): String

  def changed: Boolean = true
}

object NonePropertyProcessor extends PropertyProcessor {
  override def encrypt(message: String): String = message

  override def decrypt(input: String): String = input

  override def changed: Boolean = false
}

class PBEPropertyProcessor(encryptor: PBEEncryptor) extends PropertyProcessor {

  private val head = "ENC("
  private val tail = ")"

  override def decrypt(input: String): String = {
    if (null == input) {
      null
    } else {
      if (input.startsWith(head) && input.endsWith(tail)) {
        val msg = Strings.substringBetween(input, head, tail)
        encryptor.decrypt(msg)
      } else {
        input
      }
    }
  }

  override def encrypt(message: String): String = {
    require(null != message)
    head + encryptor.encrypt(message) + tail
  }
}
