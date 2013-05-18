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

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import java.io.StringReader
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class CsvReaderTest {

  var reader: CsvReader = null

  @BeforeClass
  def builderReader() {
    val sb = new StringBuilder()
    sb.append("a,b,c").append("\n")
    sb.append("a,\"b,b,b\",c").append("\n")
    sb.append(",,").append("\n")
    sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n")
    sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n")
    sb.append("\"\"\"\"\"\",\"test\"\n")
    sb.append("\"a\nb\",b,\"\nd\",e\n")
    reader = new CsvReader(new StringReader(sb.toString))
  }

  /**
   * Tests iterating over a reader.
   */
  @Test
  def testRead() {
    var nextLine = reader.readNext()
    assertEquals("a", nextLine(0))
    assertEquals("b", nextLine(1))
    assertEquals("c", nextLine(2))
    nextLine = reader.readNext()
    assertEquals("a", nextLine(0))
    assertEquals("b,b,b", nextLine(1))
    assertEquals("c", nextLine(2))
    nextLine = reader.readNext()
    assertEquals(3, nextLine.length)
    nextLine = reader.readNext()
    assertEquals(3, nextLine.length)
    nextLine = reader.readNext()
    assertEquals("Glen \"The Man\" Smith", nextLine(0))
    nextLine = reader.readNext()
    assertEquals("\"\"", nextLine(0))
    assertEquals("test", nextLine(1))
    nextLine = reader.readNext()
    assertEquals(4, nextLine.length)
    assertNull(reader.readNext())
  }
}
