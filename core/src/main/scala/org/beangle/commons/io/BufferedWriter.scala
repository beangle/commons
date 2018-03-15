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
package org.beangle.commons.io

import java.io.Writer

class BufferedWriter(out: Writer, bufferSize: Int = 8192) extends Writer {
  private var buffer = new Array[Char](bufferSize)
  private var pointer: Int = _

  override def write(c: Int): Unit = {
    if (pointer + 1 >= buffer.length) flushBuffer()
    buffer(pointer) = c.asInstanceOf[Char]
    pointer += 1
  }

  private def flushBuffer(): Unit = {
    if (pointer == 0) return ;
    out.write(buffer, 0, pointer);
    pointer = 0
  }

  override def write(c: Array[Char], off: Int, len: Int): Unit = {
    if (pointer + len >= buffer.length) {
      flushBuffer()
      if (len > buffer.length) {
        out.write(c, off, len);
        return
      }
    }
    System.arraycopy(c, 0, buffer, pointer, len);
    pointer += len
  }

  override def flush(): Unit = {
    flushBuffer()
    out.flush()
  }

  override def close(): Unit = {
    flushBuffer()
    out.close()
  }
}
