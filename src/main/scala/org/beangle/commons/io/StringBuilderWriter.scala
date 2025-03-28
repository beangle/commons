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

/** {@link Writer} implementation that outputs to a {@link StringBuilder}.
 * <p>
 * <strong>NOTE:</strong> This implementation, as an alternative to
 * <code>java.io.StringWriter</code>, provides an <i>un-synchronized</i> (i.e. for use in a single
 * thread) implementation for better performance. For safe usage with multiple {@link Thread}s then
 * <code>java.io.StringWriter</code> should be used.
 *
 * @author chaostone
 * @since 3.1
 */
@SerialVersionUID(1L)
class StringBuilderWriter(val builder: StringBuilder) extends Writer, Serializable {

  /** Construct a new {@link StringBuilder} instance with the specified capacity.
   *
   * @param capacity The initial capacity of the underlying { @link StringBuilder}
   */
  def this(capacity: Int = 16) = {
    this(new StringBuilder(capacity))
  }

  /** Append a single character to this Writer.
   */
  override def append(value: Char): Writer = {
    builder.append(value)
    this
  }

  /** Append a character sequence to this Writer.
   */
  override def append(value: CharSequence): Writer = {
    builder.append(value)
    this
  }

  /** Append a portion of a character sequence to the {@link StringBuilder}.
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

  /** Write a String to the {@link StringBuilder}.
   *
   * @param value The value to write
   */
  override def write(value: String): Unit =
    if (value != null) builder.append(value)

  /** Write a portion of a character array to the {@link StringBuilder}.
   */
  override def write(value: Array[Char], offset: Int, length: Int): Unit =
    if (value != null) builder.appendAll(value, offset, length)

  /** Returns {@link StringBuilder# toString ( )}.
   */
  override def toString: String = builder.toString
}
