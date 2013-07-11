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
 * @version $Id: $
 */
class CsvFormat private (val separators: Set[Char], val delimiter: Char) {

  var escape = CsvConstants.Escape

  val strictQuotes = false

  private def this(separators: Set[Char], delimiter: Char, escape: Char) {
    this(separators, delimiter)
    this.escape = escape
  }

  /**
   * <p>
   * isSeparator.
   * </p>
   *
   * @param a a char.
   * @return a boolean.
   */
  def isSeparator(a: Char): Boolean = separators.contains(a)

  /**
   * <p>
   * isDelimiter.
   * </p>
   *
   * @param a a char.
   * @return a boolean.
   */
  def isDelimiter(a: Char): Boolean = a == delimiter

  /**
   * <p>
   * isEscape.
   * </p>
   *
   * @param a a char.
   * @return a boolean.
   */
  def isEscape(a: Char): Boolean = a == escape

  /**
   * <p>
   * defaultSeparator.
   * </p>
   *
   * @return a char.
   */
  def defaultSeparator(): Char = {
    if (separators.isEmpty) CsvConstants.Comma
    else separators.head
  }
}
