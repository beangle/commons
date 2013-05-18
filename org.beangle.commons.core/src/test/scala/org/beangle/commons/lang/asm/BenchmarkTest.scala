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
package org.beangle.commons.lang.asm

import java.lang.reflect.Method
import org.beangle.commons.lang.testbean.TestBean
import org.beangle.commons.lang.time.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import BenchmarkTest._
//remove if not needed
import scala.collection.JavaConversions._

object BenchmarkTest {

  private val logger = LoggerFactory.getLogger(classOf[BenchmarkTest])
}

@Test
class BenchmarkTest {

  var testCount: Int = 1

  def testJdkReflect() {
    logger.debug("testJdkReflect...")
    val someObject = new TestBean()
    val method = classOf[TestBean].getMethod("setName", classOf[String])
    for (i <- 0 until 5) {
      val sw = new Stopwatch(true)
      for (j <- 0 until testCount) {
        method.invoke(someObject, "Unmi")
      }
      logger.debug(sw + " ")
    }
  }

  def testReflectAsm() {
    logger.debug("testReflectAsm...")
    val someObject = new TestBean()
    val access = Mirror.get(classOf[TestBean])
    for (i <- 0 until 5) {
      val begin = System.currentTimeMillis()
      for (j <- 0 until testCount) {
        access.invoke(someObject, access.getIndex("setName"), "Unmi")
      }
      logger.debug(System.currentTimeMillis() - begin + " ")
    }
  }
}
