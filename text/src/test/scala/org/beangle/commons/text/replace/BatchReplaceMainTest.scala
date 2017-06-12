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
package org.beangle.commons.text.replace

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BatchReplaceMainTest extends FunSpec with Matchers {
  describe("BatchReplace") {
    it("Test batch replace expression") {
      val clause = "<#include \"/template/head.ftl\"/>"
      val pattern = Pattern.compile("<#(.*)/>")
      val m = pattern.matcher(clause)
      val rs = m.find()
      debug(rs + "")
      debug(m.groupCount() + "")
      debug(Pattern.matches("<#(.*)/>", clause) + "")
      debug(m.group(1))
      val sb = new StringBuffer()
      m.appendReplacement(sb, "[#$1/]")
      debug(sb.toString)
      debug(Pattern.matches("template", clause) + "")
      val p = Pattern.compile("(cat)")
      val m1 = p.matcher("one cat two cats in the yard")
      val sb1 = new StringBuffer()
      while (m.find()) {
        m1.appendReplacement(sb1, "dog")
      }
      m1.appendTail(sb1)
      debug(sb1.toString)
      debug("one cat two cats in the yard".replaceAll("cat", "dog"))
      debug(clause.replaceAll("<#(.*)/>", "[#$1/]"))
      val test = "aaa    \nbbaad\n"
      val replacer = new PatternReplacer("( +?)\\n", "\n")
      debug(test)
      debug(replacer.process(test))
    }

    def debug(msg: String): Unit = {
      println(msg)
    }
  }
}

