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

/** CSV format options and builder. */
object CsvFormat {

  /** Fluent builder for CsvFormat. */
  class Builder {

    /** Quote delimiter character. */
    var delimiter: Char = CsvConstants.Quote

    /** Allowed field separator characters. */
    var separators = collection.mutable.HashSet[Char]()

    /** Escape character for quoted content. */
    var Escape: Char = CsvConstants.Escape

    /** Adds a field separator; returns this for chaining. */
    def separator(Comma: Char): Builder = {
      separators += Comma
      this
    }

    /** Sets escape character; returns this for chaining. */
    def escape(Escape: Char): Builder = {
      this.Escape = Escape
      this
    }

    /** Sets quote delimiter; returns this for chaining. */
    def delimiter(delimiter: Char): Builder = {
      this.delimiter = delimiter
      this
    }

    /** Builds the CsvFormat instance. */
    def build(): CsvFormat = {
      if (separators.isEmpty) separators.add(CsvConstants.Comma)
      new CsvFormat(separators.toSet, delimiter, Escape)
    }
  }
}

/** CSV format definition (separator, delimiter, escape).
 *
 * @author chaostone
 */
class CsvFormat private(val separators: Set[Char], val delimiter: Char) {

  /** Escape character for quoted content. */
  var escape = CsvConstants.Escape

  /** When true, quotes must strictly wrap fields. */
  val strictQuotes = false

  private def this(separators: Set[Char], delimiter: Char, escape: Char) = {
    this(separators, delimiter)
    this.escape = escape
  }

  /** Returns true if the char is a field separator. */
  def isSeparator(a: Char): Boolean = separators.contains(a)

  /** Returns true if the char is the quote delimiter. */
  def isDelimiter(a: Char): Boolean = a == delimiter

  /** Returns true if the char is the escape character. */
  def isEscape(a: Char): Boolean = a == escape

  /** Returns the primary field separator. */
  def defaultSeparator(): Char =
    if (separators.isEmpty) CsvConstants.Comma
    else separators.head
}
