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

import org.beangle.commons.csv.internal.CsvParser

import java.io.{BufferedReader, Reader}

/** Reads CSV from a Reader.
 *
 * @author chaostone
 */
class CsvReader(reader: Reader, format: CsvFormat) {

  private var hasNext: Boolean = true

  private var linesSkiped: Boolean = _

  private var skipLines: Int = 0

  private val br = new BufferedReader(reader)

  private val parser = new CsvParser(format)

  /**
   * Constructor for CsvReader.
   *
   * @param reader a [[java.io.Reader]] object.
   */
  def this(reader: Reader) = {
    this(reader, new CsvFormat.Builder().build())
  }

  /** Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   */
  private def readNextLine(): String = {
    if (!this.linesSkiped) {
      for (i <- 0 until skipLines) br.readLine()
      this.linesSkiped = true
    }
    val nextLine = br.readLine()
    if (nextLine == null) hasNext = false
    if (hasNext) nextLine else null
  }

  /** Reads next row as array of cell values; null when EOF.
   *
   * @return array of strings, or null if no more data
   */
  def readNext(): Array[String] = {
    var result: Array[String] = null
    while ( {
      val nextLine = readNextLine()
      if (!hasNext) {
        return result
      }
      val r = parser.parseLineMulti(nextLine)
      if (r.length > 0)
        if (result == null) {
          result = r
        } else {
          val t = new Array[String](result.length + r.length)
          System.arraycopy(result, 0, t, 0, result.length)
          System.arraycopy(r, 0, t, result.length, r.length)
          result = t
        }
      parser.isPending
    }) ()
    result
  }
}
