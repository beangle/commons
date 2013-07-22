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
package org.beangle.commons.csv.internal

import java.io.IOException
import org.beangle.commons.csv.CsvFormat
import scala.collection.mutable
import CsvParser._

object CsvParser {

  /**
   * Constant <code>InitialReadSize=128</code>
   */
  val InitialReadSize = 128
}

/**
 * A very simple CSV parser released under a commercial-friendly license. This
 * just implements splitting a single line into fields.
 *
 * @author chaostone
 * @version $Id: $
 */
class CsvParser(var format: CsvFormat) {

  private var pending: String = _

  private var inField: Boolean = false

  private val ignoreLeadingWhiteSpace = true

  /**
   * <p>
   * isPending.
   * </p>
   *
   * @return true if something was left over from last call(s)
   */
  def isPending(): Boolean = pending != null

  /**
   * <p>
   * parseLineMulti.
   * </p>
   *
   * @param nextLine a {@link java.lang.String} object.
   * @return an array of {@link java.lang.String} objects.
   */
  def parseLineMulti(nextLine: String): Array[String] = parseLine(nextLine, true)

  /**
   * <p>
   * parseLine.
   * </p>
   *
   * @param nextLine a {@link java.lang.String} object.
   * @return an array of {@link java.lang.String} objects.
   */
  def parseLine(nextLine: String): Array[String] = parseLine(nextLine, false)

  /**
   * Parses an incoming String and returns an array of elements.
   *
   * @param nextLine
   *          the string to parse
   * @param multi
   * @return the Comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException
   *           if bad things happen during the read
   */
  private def parseLine(nextLine: String, multi: Boolean): Array[String] = {
    if (!multi && pending != null) {
      pending = null
    }
    if (nextLine == null) {
      if (pending != null) {
        val s = pending
        pending = null
        return Array(s)
      } else {
        return null
      }
    }
    val tokensOnThisLine = new mutable.ListBuffer[String]()
    var sb = new StringBuilder(InitialReadSize)
    var inQuotes = false
    if (pending != null) {
      sb.append(pending)
      pending = null
      inQuotes = true
    }
    var i = 0
    while (i < nextLine.length) {
      val c = nextLine.charAt(i)
      if (format.isEscape(c)) {
        if (isNextCharacterEscapable(nextLine, inQuotes || inField, i)) {
          sb.append(nextLine.charAt(i + 1))
          i += 1
        }
      } else if (c == format.delimiter) {
        if (isNextCharacterEscapedQuote(nextLine, inQuotes || inField, i)) {
          sb.append(nextLine.charAt(i + 1))
          i += 1
        } else {
          inQuotes = !inQuotes
          if (!format.strictQuotes) {
            if (i > 2 && !format.isSeparator(nextLine.charAt(i - 1)) &&
              nextLine.length > (i + 1) &&
              !format.isSeparator(nextLine.charAt(i + 1))) {
              if (ignoreLeadingWhiteSpace && sb.length > 0 && isAllWhiteSpace(sb)) {
                sb = new StringBuilder(InitialReadSize)
              } else {
                sb.append(c)
              }
            }
          }
        }
        inField = !inField
      } else if (format.isSeparator(c) && !inQuotes) {
        tokensOnThisLine += sb.toString
        sb = new StringBuilder(InitialReadSize)
        inField = false
      } else {
        if (!format.strictQuotes || inQuotes) {
          sb.append(c)
          inField = true
        }
      }
      i += 1
    }
    if (inQuotes) {
      if (multi) {
        sb.append("\n")
        pending = sb.toString
        sb = null
      } else {
        throw new RuntimeException("Un-terminated Quoted field at end of CSV line")
      }
    }
    if (sb != null) {
      tokensOnThisLine += sb.toString
    }
    tokensOnThisLine.toArray
  }

  /**
   * precondition: the current character is a Quote or an Escape
   *
   * @param nextLine the current line
   * @param inQuotes true if the current context is Quoted
   * @param i current index in line
   * @return true if the following character is a Quote
   */
  private def isNextCharacterEscapedQuote(nextLine: String, inQuotes: Boolean, i: Int): Boolean = {
    inQuotes && nextLine.length > (i + 1) && format.isDelimiter(nextLine.charAt(i + 1))
  }

  /**
   * precondition: the current character is an Escape
   *
   * @param nextLine the current line
   * @param inQuotes true if the current context is Quoted
   * @param i current index in line
   * @return true if the following character is a Quote
   */
  protected def isNextCharacterEscapable(nextLine: String, inQuotes: Boolean, i: Int): Boolean = {
    inQuotes && nextLine.length > (i + 1) &&
      (format.isDelimiter(nextLine.charAt(i + 1)) || format.isEscape(nextLine.charAt(i + 1)))
  }

  /**
   * precondition: sb.length() > 0
   *
   * @param sb A sequence of characters to examine
   * @return true if every character in the sequence is whitespace
   */
  protected def isAllWhiteSpace(sb: CharSequence): Boolean = {
    val result = true
    for (i <- 0 until sb.length) {
      val c = sb.charAt(i)
      if (!Character.isWhitespace(c)) {
        return false
      }
    }
    result
  }
}
