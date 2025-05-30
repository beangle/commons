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

package org.beangle.commons.file.text

import org.beangle.commons.logging.Logging
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FormatterTest extends AnyFunSpec, Matchers, Logging {

  val lines = "package org.beangle.commons.file\r\n\r\n\t/**\r\n\n\n\n\n\n \t*\ttab2space,fixcrlf,trim_trailing_whitespace,insert_final_newline{fixlast}  \t  \r\n \t*/ \t\r\npackage object text {"
  describe("Formatter") {
    it("format") {
      val builder = new FormatterBuilder()
      builder.enableTab2space(2).enableTrimTrailingWhiteSpace().insertFinalNewline()
      val formatter = builder.build()
      val formated = formatter.format(lines)
      val expected =
        """package org.beangle.commons.file
          |
          |  /**
          |
          |   *  tab2space,fixcrlf,trim_trailing_whitespace,insert_final_newline{fixlast}
          |   */
          |package object text {
          |""".stripMargin
      assert(expected == formated)
    }
  }
}
