/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.csv

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import org.beangle.commons.csv.internal.CsvParser

/**
 * CsvReader class.
 *
 * @author chaostone
 */
class CsvReader(reader: Reader, format: CsvFormat) {

  private var hasNext: Boolean = true

  private var linesSkiped: Boolean = _

  private var skipLines: Int = 0

  private var br: BufferedReader = new BufferedReader(reader)

  private var parser: CsvParser = new CsvParser(format)

  /**
   * <p>
   * Constructor for CsvReader.
   * </p>
   *
   * @param reader a {@link java.io.Reader} object.
   */
  def this(reader: Reader) {
    this(reader, new CsvFormat.Builder().build())
  }

  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   * @throws IOException
   *           if bad things happen during the read
   */
  private def getNextLine(): String = {
    if (!this.linesSkiped) {
      for (i <- 0 until skipLines) {
        br.readLine()
      }
      this.linesSkiped = true
    }
    val nextLine = br.readLine()
    if (nextLine == null) {
      hasNext = false
    }
    if (hasNext) nextLine else null
  }

  /**
   * <p>
   * readNext.
   * </p>
   *
   * @return an array of {@link java.lang.String} objects.
   */
  def readNext(): Array[String] = {
    var result: Array[String] = null
    do {
      val nextLine = getNextLine
      if (!hasNext) {
        return result
      }
      val r = parser.parseLineMulti(nextLine)
      if (r.length > 0) {
        if (result == null) {
          result = r
        } else {
          val t = new Array[String](result.length + r.length)
          System.arraycopy(result, 0, t, 0, result.length)
          System.arraycopy(r, 0, t, result.length, r.length)
          result = t
        }
      }
    } while (parser.isPending);
    result
  }
}