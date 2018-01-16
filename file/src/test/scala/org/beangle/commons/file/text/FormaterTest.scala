package org.beangle.commons.file.text

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FormaterTest extends FunSpec with Matchers {

  val lines = "package org.beangle.commons.file\r\n\r\n\t/**\r\n\n\n\n\n\n \t*\ttab2space,fixcrlf,trim_trailing_whitespace,insert_final_newline{fixlast}  \t  \r\n \t*/ \t\r\npackage object text {"
  describe("Formater") {
    it("format") {
      val builder = new FormaterBuilder()
      builder.enableTab2space(4).enableTrimTrailingWhiteSpace().insertFinalNewline()
      val formater = builder.build()
      val formated = formater.format(lines)
      println(lines)
      print(formated)
    }
  }
}
