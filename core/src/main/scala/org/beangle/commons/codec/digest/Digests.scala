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

package org.beangle.commons.codec.digest

import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.lang.Charsets.*

import java.security.MessageDigest

/** Digest tools
  */
object Digests {

  def md5: MessageDigest = MessageDigest.getInstance("MD5")

  def sha1: MessageDigest = MessageDigest.getInstance("SHA-1")

  def md5Hex(bytes: Array[Byte]): String = Hex.encode(md5.digest(bytes))

  def md5Hex(string: String): String = Hex.encode(md5.digest(string.getBytes(UTF_8)))

  def sha1Hex(bytes: Array[Byte]): String = Hex.encode(sha1.digest(bytes))

  def sha1Hex(string: String): String = Hex.encode(sha1.digest(string.getBytes(UTF_8)))
}
