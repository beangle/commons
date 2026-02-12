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

package org.beangle.commons.csv

import org.beangle.commons.csv.CsvWriter.*
import org.beangle.commons.lang.Throwables

import java.io.{Closeable, IOException, Writer}

/** CsvWriter constants and factory. */
object CsvWriter {

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

/** Writes CSV rows to a Writer.
 *
 * @param writer the output writer
 * @param format CSV format (delimiter, quote, escape)
 * @author chaostone
 */
class CsvWriter(val writer: Writer,
                val format: CsvFormat = new CsvFormat.Builder().escape(NoEscapeChar).build()) extends Closeable {

  /**
   * Constant `InitialStringSize=128`
   */
  private val InitialStringSize = 128
  private val lineEnd: String = "\n"

  /** Writes the data as CSV. Supports Array or Iterable of Array.
   *
   * @param data the data to write (Array or Iterable[Array])
   */
  def write(data: Any): Unit = {
    data match {
      case a: Array[_] => writeOne(a)
      case lines: Iterable[_] => for (line <- lines) writeOne(line.asInstanceOf[Array[_]])
    }
  }

  /**
   * write.
   */
  private def writeOne(nextLine: Array[_]): Unit = {
    if (nextLine == null) return
    val sb = new StringBuilder(InitialStringSize)
    for (i <- nextLine.indices) {
      if (i != 0) sb.append(format.defaultSeparator())
      val nextElem = nextLine(i)
      if (null != nextElem) {
        val nextElement = nextElem.toString
        if (!format.isDelimiter(NoQuoteChar)) sb.append(format.delimiter)
        sb.append(if (containsSpecialChar(nextElement)) processLine(nextElement) else nextElement)
        if (!format.isDelimiter(NoQuoteChar)) sb.append(format.delimiter)
      }
    }
    sb.append(lineEnd)
    writer.write(sb.toString)
  }

  private def containsSpecialChar(line: String): Boolean =
    line.indexOf(format.delimiter) != -1 || line.indexOf(format.delimiter) != -1

  /** Processes a line element for CSV (escapes delimiter and escape chars).
   *
   * @param nextElement the element string
   * @return StringBuilder with processed content
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

  /** Flushes buffered output to the underlying writer. */
  def flush(): Unit =
    writer.flush()

  /** Closes the writer (flush then close). */
  def close(): Unit =
    try {
      flush()
      writer.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
}
