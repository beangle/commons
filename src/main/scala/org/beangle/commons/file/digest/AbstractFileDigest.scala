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

package org.beangle.commons.file.digest

import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.io.IOs

import java.io.{File, FileInputStream}
import java.security.MessageDigest

/** Base for file digest (hex string of hash). */
abstract class AbstractFileDigest {

  /** Computes digest of file as hex string; empty if file not found. */
  final def digest(file: File): String = {
    if (!file.exists()) return "";

    val md = getAlgorithm()
    val buffer = new Array[Byte](4 * 1024)
    var count = 0
    val input = new FileInputStream(file)
    var n = input.read(buffer)
    val eof = -1

    while (eof != n) {
      md.update(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    IOs.close(input)
    Hex.encode(md.digest())
  }

  /** Returns the MessageDigest instance. */
  protected def getAlgorithm(): MessageDigest
}
