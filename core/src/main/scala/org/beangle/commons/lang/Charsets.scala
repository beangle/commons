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

package org.beangle.commons.lang

import java.nio.charset.Charset

object Charsets {

  /** US-ASCII: seven-bit ASCII, the Basic Latin block of the Unicode character set (ISO646-US).
    */
  val US_ASCII: Charset = Charset.forName("US-ASCII")

  /** ISO-8859-1: ISO Latin Alphabet Number 1 (ISO-LATIN-1).
    */
  val ISO_8859_1: Charset = Charset.forName("ISO-8859-1")

  /** UTF-8: eight-bit UCS Transformation Format.
    */
  val UTF_8: Charset = Charset.forName("UTF-8")

  /** UTF-16BE: sixteen-bit UCS Transformation Format, big-endian byte order.
    */
  val UTF_16BE: Charset = Charset.forName("UTF-16BE")

  /** UTF-16LE: sixteen-bit UCS Transformation Format, little-endian byte order.
    */
  val UTF_16LE: Charset = Charset.forName("UTF-16LE")

  /** UTF-16: sixteen-bit UCS Transformation Format, byte order identified by an optional
    * byte-order-mark.
    */
  val UTF_16: Charset = Charset.forName("UTF-16")
}
