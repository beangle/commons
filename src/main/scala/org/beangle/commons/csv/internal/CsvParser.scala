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

package org.beangle.commons.csv.internal

import org.beangle.commons.csv.CsvFormat

import scala.collection.mutable


/** Simple CSV parser that splits a single line into fields.
 *
 * @author chaostone
 */
class CsvParser(var format: CsvFormat) {
  private val InitialReadSize = 128
  private var pending: String = _
  private var inField: Boolean = false
  private val ignoreLeadingWhiteSpace = true

  /** Returns true if there is pending content from a previous multi-line read. */
  def isPending: Boolean = pending != null

  /** Parses a line, possibly spanning multiple physical lines (handles quoted newlines).
   *
   * @param nextLine the line(s) to parse
   * @return the field array, or null if nextLine is null
   */
  def parseLineMulti(nextLine: String): Array[String] = parseLine(nextLine, true)

  /** Parses a single line into fields.
   *
   * @param nextLine the line to parse
   * @return the field array, or null if nextLine is null
   */
  def parseLine(nextLine: String): Array[String] = parseLine(nextLine, false)

  /** Parses the string and returns tokenized fields.
   *
   * @param nextLine the string to parse
   * @param multi    if true, handles quoted newlines (multi-line fields)
   * @return the field array, or null if nextLine is null
   */
  private def parseLine(nextLine: String, multi: Boolean): Array[String] = {
    if (!multi && pending != null)
      pending = null
    if (nextLine == null)
      if (pending != null) {
        val s = pending
        pending = null
        return Array(s)
      } else
        return null
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
          if (!format.strictQuotes)
            if (i > 2 && !format.isSeparator(nextLine.charAt(i - 1)) &&
              nextLine.length > (i + 1) &&
              !format.isSeparator(nextLine.charAt(i + 1)))
              if (ignoreLeadingWhiteSpace && sb.length > 0 && isAllWhiteSpace(sb))
                sb = new StringBuilder(InitialReadSize)
              else
                sb.append(c)
        }
        inField = !inField
      } else if (format.isSeparator(c) && !inQuotes) {
        tokensOnThisLine += sb.toString
        sb = new StringBuilder(InitialReadSize)
        inField = false
      } else if (!format.strictQuotes || inQuotes) {
        sb.append(c)
        inField = true
      }
      i += 1
    }
    if (inQuotes)
      if (multi) {
        sb.append("\n")
        pending = sb.toString
        sb = null
      } else
        throw new RuntimeException("Un-terminated Quoted field at end of CSV line")
    if (sb != null)
      tokensOnThisLine += sb.toString
    tokensOnThisLine.toArray
  }

  /**
   * precondition: the current character is a Quote or an Escape
   */
  private def isNextCharacterEscapedQuote(nextLine: String, inQuotes: Boolean, i: Int): Boolean =
    inQuotes && nextLine.length > (i + 1) && format.isDelimiter(nextLine.charAt(i + 1))

  /**
   * precondition: the current character is an Escape
   */
  protected def isNextCharacterEscapable(nextLine: String, inQuotes: Boolean, i: Int): Boolean =
    inQuotes && nextLine.length > (i + 1) &&
      (format.isDelimiter(nextLine.charAt(i + 1)) || format.isEscape(nextLine.charAt(i + 1)))

  /**
   * precondition: sb.length() > 0
   */
  protected def isAllWhiteSpace(sb: CharSequence): Boolean = {
    val result = true
    var i = 0
    while (i < sb.length()) {
      val c = sb.charAt(i)
      if !Character.isWhitespace(c) then return false
      i += 1
    }
    result
  }
}
