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
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter
import java.util.ArrayList
import java.util.List
import org.beangle.commons.lang.SystemInfo
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class CsvWriterTest {

  private def invokeWriter(args: Array[String]): String = {
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter('\'').build())
    csvw.write(args)
    csvw.close()
    sw.toString
  }

  private def invokeNoEscapeWriter(args: Array[String]): String = {
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter('\'').escape(CsvWriter.NoEscapeChar)
      .build())
    csvw.write(args)
    csvw.close()
    sw.toString
  }

  def correctlyParseNullString() {
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().escape('\'').build())
    csvw.write(null.asInstanceOf[Array[String]])
    csvw.close()
    assertEquals(0, sw.toString.length)
  }

  /**
   * Tests parsing individual lines.
   *
   * @throws IOException
   *           if the reader fails.
   */
  @Test
  def testParseLine() {
    val normal = Array("a", "b", "c")
    var output = invokeWriter(normal)
    assertEquals(output, "'a','b','c'\n")
    val quoted = Array("a", "b,b,b", "c")
    output = invokeWriter(quoted)
    assertEquals("'a','b,b,b','c'\n", output)
    val empty = Array[String]()
    output = invokeWriter(empty)
    assertEquals("\n", output)
    val multiline = Array("This is a \n multiline entry", "so is \n this")
    output = invokeWriter(multiline)
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output)
    val quoteLine = Array("This is a \" multiline entry", "so is \n this")
    output = invokeWriter(quoteLine)
    assertEquals("'This is a \" multiline entry','so is \n this'\n", output)
  }

  @Test
  def parseLineWithBothEscapeAndQuoteChar() {
    val quoteLine = Array("This is a 'multiline' entry", "so is \n this")
    val output = invokeWriter(quoteLine)
    assertEquals("'This is a \\'multiline\\' entry','so is \n this'\n", output)
  }

  /**
   * Tests parsing individual lines.
   *
   * @throws IOException
   *           if the reader fails.
   */
  @Test
  def testParseLineWithNoEscapeChar() {
    val normal = Array("a", "b", "c")
    var output = invokeNoEscapeWriter(normal)
    assertEquals("'a','b','c'\n", output)
    val quoted = Array("a", "b,b,b", "c")
    output = invokeNoEscapeWriter(quoted)
    assertEquals("'a','b,b,b','c'\n", output)
    val empty = Array[String]()
    output = invokeNoEscapeWriter(empty)
    assertEquals("\n", output)
    val multiline = Array("This is a \n multiline entry", "so is \n this")
    output = invokeNoEscapeWriter(multiline)
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output)
  }

  @Test
  def parseLineWithNoEscapeCharAndQuotes() {
    val quoteLine = Array("This is a \" 'multiline' entry", "so is \n this")
    val output = invokeNoEscapeWriter(quoteLine)
    assertEquals("'This is a \" 'multiline' entry','so is \n this'\n", output)
  }

  /**
   * Test parsing from to a list.
   *
   * @throws IOException
   *           if the reader fails.
   */
  @Test
  def testParseAll() {
    val allElements = new ArrayList[Array[String]]()
    val line1 = "Name#Phone#Email".split("#")
    val line2 = "Glen#1234#glen@abcd.com".split("#")
    val line3 = "John#5678#john@efgh.com".split("#")
    allElements.add(line1)
    allElements.add(line2)
    allElements.add(line3)
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw)
    csvw.write(allElements)
    csvw.close()
    val result = sw.toString
    val lines = result.split("\n")
    assertEquals(3, lines.length)
  }

  /**
   * Tests the option of having omitting quotes in the output stream.
   *
   * @throws IOException
   *           if bad things happen
   */
  @Test
  def testNoQuoteChars() {
    val line = Array("Foo", "Bar", "Baz")
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
      .build())
    csvw.write(line)
    val result = sw.toString
    csvw.close()
    assertEquals("Foo,Bar,Baz\n", result)
  }

  /**
   * Tests the option of having omitting quotes in the output stream.
   *
   * @throws IOException
   *           if bad things happen
   */
  @Test
  def testNoQuoteCharsAndNoEscapeChars() {
    val line = Array("Foo", "Bar", "Baz")
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
      .escape(CsvWriter.NoEscapeChar)
      .build())
    csvw.write(line)
    val result = sw.toString
    csvw.close()
    assertEquals("Foo,Bar,Baz\n", result)
  }

  /**
   * Test null values.
   *
   * @throws IOException
   *           if bad things happen
   */
  @Test
  def testNullValues() {
    val line = Array("Foo", null, "Bar", "baz")
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw)
    csvw.write(line)
    val result = sw.toString
    csvw.close()
    assertEquals("\"Foo\",,\"Bar\",\"baz\"\n", result)
  }

  @Test
  def testStreamFlushing() {
    val WRITE_FILE = SystemInfo.getTmpDir + "/myfile.csv"
    val nextLine = Array("aaaa", "bbbb", "cccc", "dddd")
    val fileWriter = new FileWriter(WRITE_FILE)
    val writer = new CsvWriter(fileWriter)
    writer.write(nextLine)
    writer.close()
  }

  @Test
  def testAlternateEscapeChar() {
    val line = Array("Foo", "bar's")
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().escape('\'').build())
    csvw.write(line)
    csvw.close()
    assertEquals("\"Foo\",\"bar's\"\n", sw.toString)
  }

  @Test
  def testNoQuotingNoEscaping() {
    val line = Array("\"Foo\",\"Bar\"")
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
      .escape(CsvWriter.NoEscapeChar)
      .build())
    csvw.write(line)
    assertEquals("\"Foo\",\"Bar\"\n", sw.toString)
    csvw.close()
  }

  @Test
  def testNestedQuotes() {
    val data = Array("\"\"", "test")
    val oracle = "\"\"\"\",\"test\"\n"
    var writer: CsvWriter = null
    var tempFile: File = null
    var fwriter: FileWriter = null
    try {
      tempFile = File.createTempFile("csvWriterTest", ".csv")
      tempFile.deleteOnExit()
      fwriter = new FileWriter(tempFile)
      writer = new CsvWriter(fwriter)
    } catch {
      case e: IOException =>
    }
    writer.write(data)
    writer.close()
    try {
      fwriter.flush()
    } catch {
      case e: IOException =>
    }
    var in: FileReader = null
    try {
      in = new FileReader(tempFile)
    } catch {
      case e: FileNotFoundException =>
    }
    val fileContents = new StringBuilder(CsvWriter.InitialStringSize)
    try {
      var ch = in.read()
      while (ch != -1) {
        fileContents.append(ch.toChar)
        ch = in.read()
      }
      in.close()
    } catch {
      case e: IOException =>
    }
    assertEquals(oracle, fileContents.toString)
  }
}
