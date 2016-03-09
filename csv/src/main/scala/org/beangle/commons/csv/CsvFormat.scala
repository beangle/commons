/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
import CsvFormat._

object CsvFormat {

  class Builder {

    var delimiter: Char = CsvConstants.Quote

    var separators = collection.mutable.HashSet[Char]()

    var Escape: Char = CsvConstants.Escape

    def separator(Comma: Char): Builder = {
      separators += Comma
      this
    }

    def escape(Escape: Char): Builder = {
      this.Escape = Escape
      this
    }

    def delimiter(delimiter: Char): Builder = {
      this.delimiter = delimiter
      this
    }

    def build(): CsvFormat = {
      if (separators.isEmpty) separators.add(CsvConstants.Comma)
      new CsvFormat(separators.toSet, delimiter, Escape)
    }
  }
}

/**
 * csv format definition
 *
 * @author chaostone
 */
class CsvFormat private (val separators: Set[Char], val delimiter: Char) {

  var escape = CsvConstants.Escape

  val strictQuotes = false

  private def this(separators: Set[Char], delimiter: Char, escape: Char) {
    this(separators, delimiter)
    this.escape = escape
  }

  /**
   * isSeparator.
   */
  def isSeparator(a: Char): Boolean = separators.contains(a)

  /**
   * isDelimiter.
   */
  def isDelimiter(a: Char): Boolean = a == delimiter

  /**
   * isEscape.
   */
  def isEscape(a: Char): Boolean = a == escape

  /**
   * defaultSeparator.
   */
  def defaultSeparator(): Char = {
    if (separators.isEmpty) CsvConstants.Comma
    else separators.head
  }
}
