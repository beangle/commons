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
      System.out.println(Reader.readInfo(in))
      val csv = new File(fileName.replace("dbf", "csv"))
//      Reader.writeToCsv(in, csv, Charset.forName("GB18030"))
    }
  }
}
