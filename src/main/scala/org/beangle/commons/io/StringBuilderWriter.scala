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

package org.beangle.commons.io

import java.io.Writer

/** Writer that outputs to StringBuilder (single-thread, faster than StringWriter).
 *
 * @author chaostone
 * @since 3.1
 */
class StringBuilderWriter(val builder: StringBuilder) extends Writer, Serializable {

  def this(capacity: Int = 16) = {
    this(new StringBuilder(capacity))
  }

  /** Appends a single character to this Writer. */
  override def append(value: Char): Writer = {
    builder.append(value)
    this
  }

  /** Appends a character sequence to this Writer. */
  override def append(value: CharSequence): Writer = {
    builder.append(value)
    this
  }

  /** Appends a subsequence to the underlying StringBuilder.
   *
   * @param value the character sequence
   * @param start start index (inclusive)
   * @param end   end index (exclusive)
   */
  override def append(value: CharSequence, start: Int, end: Int): Writer = {
    builder.append(value, start, end)
    this
  }

  /** Closing this writer has no effect.
   */
  override def close(): Unit = {
  }

  /** Flushing this writer has no effect.
   */
  override def flush(): Unit = {
  }

  /** Write a String to the [[StringBuilder]].
   *
   * @param value The value to write
   */
  override def write(value: String): Unit =
    if (value != null) builder.append(value)

  /** Writes a portion of a character array to the StringBuilder.
   *
   * @param value  the character array
   * @param offset the start offset
   * @param length the number of characters to write
   */
  override def write(value: Array[Char], offset: Int, length: Int): Unit =
    if (value != null) builder.appendAll(value, offset, length)

  /** Returns the underlying StringBuilder's string. */
  override def toString: String = builder.toString
}
