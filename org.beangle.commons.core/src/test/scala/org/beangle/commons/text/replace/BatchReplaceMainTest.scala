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
package org.beangle.commons.text.replace

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class BatchReplaceMainTest {

  var logger: Logger = LoggerFactory.getLogger(classOf[BatchReplaceMainTest])

  def test() {
    val clause = "<#include \"/template/head.ftl\"/>"
    val pattern = Pattern.compile("<#(.*)/>")
    val m = pattern.matcher(clause)
    logger.debug(m.find() + "")
    logger.debug(m.groupCount() + "")
    logger.debug(Pattern.matches("<#(.*)/>", clause) + "")
    logger.debug(m.group(1))
    val sb = new StringBuffer()
    m.appendReplacement(sb, "[#$1/]")
    logger.debug(sb.toString)
    logger.debug(Pattern.matches("template", clause) + "")
    val p = Pattern.compile("(cat)")
    val m1 = p.matcher("one cat two cats in the yard")
    val sb1 = new StringBuffer()
    while (m.find()) {
      m1.appendReplacement(sb1, "dog")
    }
    m1.appendTail(sb1)
    logger.debug(sb1.toString)
    logger.debug("one cat two cats in the yard".replaceAll("cat", "dog"))
    logger.debug(clause.replaceAll("<#(.*)/>", "[#$1/]"))
    val test = "aaa    \nbbaad\n"
    val replacer = new Replacer("( +?)\\n", "\n")
    logger.debug(test)
    logger.debug(replacer.process(test))
  }
}
