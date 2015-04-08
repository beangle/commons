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
package org.beangle.commons.csv

import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.io.Writer
import org.beangle.commons.lang.Throwables
import CsvWriter._

object CsvWriter {

  /**
   * Constant <code>InitialStringSize=128</code>
   */
  val InitialStringSize = 128

  /**
   * The Quote constant to use when you wish to suppress all quoting.
   */
  val NoQuoteChar = '\u0000'
 
   /**
   * The Escape constant to use when you wish to suppress all escaping.
   */
  val NoEscapeChar = '\u0000'
   /**
   * Default line terminator uses platform encoding.
   */
  val DefaultLineEnd = "\n"
}

/**
 * CsvWriter class.
 *
 * @author chaostone
 */
class CsvWriter(val writer: Writer, val format: CsvFormat = new CsvFormat.Builder().escape(NoEscapeChar)
  .build()) extends Closeable {

  private var lineEnd: String = "\n"

  /**
   * write.
   */
  def write(allLines: Seq[Array[String]]) {
    for (line <- allLines) write(line)
  }

  /**
   * write.
   */
  def write(nextLine: Array[String]) {
    if (nextLine == null) return
    val sb = new StringBuilder(InitialStringSize)
    for (i <- 0 until nextLine.length) {
      if (i != 0) {
        sb.append(format.defaultSeparator())
      }
      val nextElement = nextLine(i)
      if (null != nextElement) {
        if (!format.isDelimiter(NoQuoteChar)) sb.append(format.delimiter)
        sb.append(if (containsSpecialChar(nextElement)) processLine(nextElement) else nextElement)
        if (!format.isDelimiter(NoQuoteChar)) sb.append(format.delimiter)
      }
    }
    sb.append(lineEnd)
    writer.write(sb.toString)
  }

  private def containsSpecialChar(line: String): Boolean = {
    line.indexOf(format.delimiter) != -1 || line.indexOf(format.delimiter) != -1
  }

  /**
   * processLine.
   *
   * @param nextElement a {@link java.lang.String} object.
   * @return a {@link java.lang.StringBuilder} object.
   */
  protected def processLine(nextElement: String): StringBuilder = {
    val sb = new StringBuilder(InitialStringSize)
    for (j <- 0 until nextElement.length) {
      val nextChar = nextElement.charAt(j)
      if (format.escape != NoEscapeChar && nextChar == format.delimiter) {
        sb.append(format.escape).append(nextChar)
      } else if (format.escape != NoEscapeChar && nextChar == format.escape) {
        sb.append(format.escape).append(nextChar)
      } else {
        sb.append(nextChar)
      }
    }
    sb
  }

  /**
   * flush.
   */
  def flush() {
    writer.flush()
  }

  /**
   * close.
   */
  def close() {
    try {
      flush()
      writer.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
  }

}
