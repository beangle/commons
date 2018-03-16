/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.csv

import java.io.StringReader
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CsvReaderTest extends FunSpec with Matchers {

  var reader: CsvReader = buildReader

  def buildReader(): CsvReader = {
    val sb = new StringBuilder()
    sb.append("a,b,c").append("\n")
    sb.append("a,\"b,b,b\",c").append("\n")
    sb.append(",,").append("\n")
    sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n")
    sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n")
    sb.append("\"\"\"\"\"\",\"test\"\n")
    sb.append("\"a\nb\",b,\"\nd\",e\n")
    new CsvReader(new StringReader(sb.toString))
  }

  /**
   * Tests iterating over a reader.
   */
  describe("CsvReader") {
    it("Read next") {
      var nextLine = reader.readNext()
      nextLine(0) should equal("a")
      nextLine(1) should equal("b")
      nextLine(2) should equal("c")
      nextLine = reader.readNext()
      nextLine(0) should equal("a")
      nextLine(1) should equal("b,b,b")
      nextLine(2) should equal("c")
      nextLine = reader.readNext()
      nextLine.length should be(3)
      nextLine = reader.readNext()
      nextLine.length should be(3)
      nextLine = reader.readNext()
      nextLine(0) should equal("Glen \"The Man\" Smith")
      nextLine = reader.readNext()
      nextLine(0) should equal("\"\"")
      nextLine(1) should equal("test")
      nextLine = reader.readNext()
      nextLine.length should be(4)
      reader.readNext() should be(null)
    }
  }
}
