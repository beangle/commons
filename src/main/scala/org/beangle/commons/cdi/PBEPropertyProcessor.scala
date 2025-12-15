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

/** 可以对属性值进行解密的Processor
 * 使用类似jasypt的方式进行解密
 */
object PBEPropertyProcessor {
  def jasypt(): PropertyProcessor = {
    var password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD")
    if (null == password) {
      password = System.getProperty("jasypt.encryptor.password")
    }
    if Strings.isEmpty(password) then PropertyProcessor.None
    else PBEPropertyProcessor(PBEEncryptor.random(password))
  }
}

class PBEPropertyProcessor(encryptor: PBEEncryptor) extends PropertyProcessor {

  private val head = "ENC("
  private val tail = ")"

  override def process(key: String, value: String): String = {
    if (null == value) {
      null
    } else {
      if (value.startsWith(head) && value.endsWith(tail)) {
        val msg = Strings.substringBetween(value, head, tail)
        encryptor.decrypt(msg)
      } else {
        value
      }
    }
  }

  def encrypt(message: String): String = {
    require(null != message)
    head + encryptor.encrypt(message) + tail
  }
}
