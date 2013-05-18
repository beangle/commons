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
package org.beangle.commons.csv

import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.io.Writer
import java.util.List
import org.beangle.commons.lang.Throwables
import CsvWriter._
//remove if not needed
import scala.collection.JavaConversions._

object CsvWriter {

  /**
   * Constant <code>InitialStringSize=128</code>
   */
  val InitialStringSize = 128

  /**
   * The Quote constant to use when you wish to suppress all quoting.
   */
  val NoQuoteChar = ' '

  /**
   * The Escape constant to use when you wish to suppress all escaping.
   */
  val NoEscapeChar = ' '

  /**
   * Default line terminator uses platform encoding.
   */
  val DefaultLineEnd = "\n"
}

/**
 * <p>
 * CsvWriter class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class CsvWriter(val writer: Writer, val format: CsvFormat = new CsvFormat.Builder().escape(NoEscapeChar)
  .build()) extends Closeable {

  private var rawWriter: Writer = writer

  private var pw: PrintWriter = new PrintWriter(writer)

  private var lineEnd: String = "\n"

  /**
   * <p>
   * write.
   * </p>
   *
   * @param allLines a {@link java.util.List} object.
   */
  def write(allLines: List[Array[String]]) {
    for (line <- allLines) write(line)
  }

  /**
   * <p>
   * write.
   * </p>
   *
   * @param nextLine an array of {@link java.lang.String} objects.
   */
  def write(nextLine: Array[String]) {
    if (nextLine == null) return
    val sb = new StringBuilder(InitialStringSize)
    for (i <- 0 until nextLine.length) {
      if (i != 0) {
        sb.append(format.defaultSeparator())
      }
      val nextElement = nextLine(i)
      if (nextElement == null) //continue
        if (!format.isDelimiter(NoQuoteChar)) sb.append(format.getDelimiter)
      sb.append(if (stringContainsSpecialCharacters(nextElement)) processLine(nextElement) else nextElement)
      if (!format.isDelimiter(NoQuoteChar)) sb.append(format.getDelimiter)
    }
    sb.append(lineEnd)
    pw.write(sb.toString)
  }

  private def stringContainsSpecialCharacters(line: String): Boolean = {
    line.indexOf(format.getDelimiter) != -1 || line.indexOf(format.getDelimiter) != -1
  }

  /**
   * <p>
   * processLine.
   * </p>
   *
   * @param nextElement a {@link java.lang.String} object.
   * @return a {@link java.lang.StringBuilder} object.
   */
  protected def processLine(nextElement: String): StringBuilder = {
    val sb = new StringBuilder(InitialStringSize)
    for (j <- 0 until nextElement.length) {
      val nextChar = nextElement.charAt(j)
      if (format.getEscape != NoEscapeChar && nextChar == format.getDelimiter) {
        sb.append(format.getEscape).append(nextChar)
      } else if (format.getEscape != NoEscapeChar && nextChar == format.getEscape) {
        sb.append(format.getEscape).append(nextChar)
      } else {
        sb.append(nextChar)
      }
    }
    sb
  }

  /**
   * <p>
   * flush.
   * </p>
   *
   * @throws java.io.IOException if any.
   */
  def flush() {
    pw.flush()
  }

  /**
   * <p>
   * close.
   * </p>
   *
   * @throws java.io.IOException if any.
   */
  def close() {
    try {
      flush()
      pw.close()
      rawWriter.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
  }

  /**
   * <p>
   * checkError.
   * </p>
   *
   * @return a boolean.
   */
  def checkError(): Boolean = pw.checkError()
}
