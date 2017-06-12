/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.dbf;

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import java.io.File
import java.nio.charset.Charset

@RunWith(classOf[JUnitRunner])
class ReaderTest extends FunSpec with Matchers {

  describe("Reader") {
    it("readinfo") {
      val fileName = "/home/chaostone/buf/XL_2013_10.dbf"
      val in = new File(fileName)
      if (in.exists) {
        println(Reader.readInfo(in))
        val csv = new File(fileName.replace("dbf", "csv"))
        Reader.writeToCsv(in, csv, Charset.forName("GB18030"))
      }
    }
  }
}
