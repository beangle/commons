/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.io

import java.io._
import java.util.ArrayList
import java.util.List
//remove if not needed
import scala.collection.JavaConversions._

object IOs {

  private val DefaultBufferSize = 1024 * 4

  private val Eof = -1

  /**
   * Copy bytes from a <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 3.1
   */
  def copy(input: InputStream, output: OutputStream): Long = {
    val buffer = new Array[Byte](DefaultBufferSize)
    var count = 0
    var n = input.read(buffer)
    while (Eof != n) {
      output.write(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    count
  }

  /**
   * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 3.1
   */
  def copy(input: Reader, output: Writer): Long = {
    val buffer = new Array[Char](DefaultBufferSize)
    var count = 0
    var n = input.read(buffer)
    while (Eof != n) {
      output.write(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    count
  }

  /**
   * Get the contents of a <code>Reader</code> as a list of Strings,
   * one entry per line.
   * <p>
   *
   * @param input the <code>Reader</code> to read from, not null
   * @return the list of Strings, never null
   * @throws IOException if an I/O error occurs
   * @since 1.1
   */
  def readLines(input: Reader): List[String] = {
    val reader = toBufferedReader(input)
    val list = new ArrayList[String]()
    var line = reader.readLine()
    while (line != null) {
      list.add(line)
      line = reader.readLine()
    }
    list
  }

  def readLines(input: InputStream): List[String] = readLines(new InputStreamReader(input))

  def close(closeable: Closeable) {
    try {
      if (closeable != null) {
        closeable.close()
      }
    } catch {
      case ioe: IOException =>
    }
  }

  private def toBufferedReader(reader: Reader): BufferedReader = {
    if (reader.isInstanceOf[BufferedReader]) reader.asInstanceOf[BufferedReader] else new BufferedReader(reader)
  }
}
