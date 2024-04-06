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

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter
import org.beangle.commons.lang.SystemInfo
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CsvWriterTest extends AnyFunSpec with Matchers {

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

  def correctlyParseNullString(): Unit = {
    val sw = new StringWriter()
    val csvw = new CsvWriter(sw, new CsvFormat.Builder().escape('\'').build())
    csvw.write(null.asInstanceOf[Array[String]])
    csvw.close()
    assert(0 == sw.toString.length)
  }

  /**
   * Tests parsing individual lines.
   */
  describe("CsvWriter") {
    it("TestParseLine") {
      invokeWriter(Array("a", "b", "c")) should equal("'a','b','c'\n")
      invokeWriter(Array("a", "b,b,b", "c")) should equal("'a','b,b,b','c'\n")
      invokeWriter(Array[String]()) should equal("\n")
      invokeWriter(Array("This is a \n multiline entry", "so is \n this")) should equal("'This is a \n multiline entry','so is \n this'\n")
      invokeWriter(Array("This is a \" multiline entry", "so is \n this")) should equal("'This is a \" multiline entry','so is \n this'\n")
    }

    it("parseLineWithBothEscapeAndQuoteChar") {
      invokeWriter(Array("This is a 'multiline' entry", "so is \n this")) should equal("'This is a \\'multiline\\' entry','so is \n this'\n")
    }

    /**
     * Tests parsing individual lines.
     */
    it("testParseLineWithNoEscapeChar") {
      invokeNoEscapeWriter(Array("a", "b", "c")) should equal("'a','b','c'\n")
      invokeNoEscapeWriter(Array("a", "b,b,b", "c")) should equal("'a','b,b,b','c'\n")
      invokeNoEscapeWriter(Array[String]()) should equal("\n")
      invokeNoEscapeWriter(Array("This is a \n multiline entry", "so is \n this")) should equal("'This is a \n multiline entry','so is \n this'\n")
    }

    it("parseLineWithNoEscapeCharAndQuotes") {
      invokeNoEscapeWriter(Array("This is a \" 'multiline' entry", "so is \n this")) should equal("'This is a \" 'multiline' entry','so is \n this'\n")
    }

    /**
     * Test parsing from to a list.
     */
    it("testParseAll") {
      val elements = new collection.mutable.ListBuffer[Array[String]]()
      val line1 = "Name#Phone#Email".split("#")
      val line2 = "Glen#1234#glen@abcd.com".split("#")
      val line3 = "John#5678#john@efgh.com".split("#")
      elements += line1
      elements += line2
      elements += line3
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw)
      csvw.write(elements)
      csvw.close()
      val result = sw.toString
      val lines = result.split("\n")
      lines.length should be(3)
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     */
    it("testNoQuoteChars") {
      val line = Array("Foo", "Bar", "Baz")
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
        .build())
      csvw.write(line)
      val result = sw.toString
      csvw.close()
      result should be("Foo,Bar,Baz\n")
    }

    /**
     * Tests the option of having omitting quotes in the output stream.
     *
     */
    it("testNoQuoteCharsAndNoEscapeChars") {
      val line = Array("Foo", "Bar", "Baz")
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
        .escape(CsvWriter.NoEscapeChar)
        .build())
      csvw.write(line)
      val result = sw.toString
      csvw.close()
      result should equal("Foo,Bar,Baz\n")
    }

    /**
     * Test null values.
     *
     */
    it("testNullValues") {
      val line = Array("Foo", null, "Bar", "baz")
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw)
      csvw.write(line)
      val result = sw.toString
      csvw.close()
      result should equal("\"Foo\",,\"Bar\",\"baz\"\n")
    }

    it("testStreamFlushing") {
      val WRITE_FILE = SystemInfo.tmpDir + "/myfile.csv"
      val nextLine = Array("aaaa", "bbbb", "cccc", "dddd")
      val fileWriter = new FileWriter(WRITE_FILE)
      val writer = new CsvWriter(fileWriter)
      writer.write(nextLine)
      writer.close()
    }

    it("testAlternateEscapeChar") {
      val line = Array("Foo", "bar's")
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw, new CsvFormat.Builder().escape('\'').build())
      csvw.write(line)
      csvw.close()
      sw.toString should equal("\"Foo\",\"bar's\"\n")
    }

    it("testNoQuotingNoEscaping") {
      val line = Array("\"Foo\",\"Bar\"")
      val sw = new StringWriter()
      val csvw = new CsvWriter(sw, new CsvFormat.Builder().delimiter(CsvWriter.NoQuoteChar)
        .escape(CsvWriter.NoEscapeChar)
        .build())
      csvw.write(line)
      sw.toString should equal("\"Foo\",\"Bar\"\n")
      csvw.close()
    }

    it("testNestedQuotes") {
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
      }
      catch {
        case e: IOException =>
      }
      writer.write(data)
      writer.close()
      try
        fwriter.flush()
      catch {
        case e: IOException =>
      }
      var in: FileReader = null
      try
        in = new FileReader(tempFile)
      catch {
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
      }
      catch {
        case e: IOException =>
      }
      oracle should equal(fileContents.toString)
    }
  }
}
