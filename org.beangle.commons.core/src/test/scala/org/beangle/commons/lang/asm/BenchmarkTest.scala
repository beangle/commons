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
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import BenchmarkTest._

object BenchmarkTest {

  private val logger = LoggerFactory.getLogger(classOf[BenchmarkTest])
}

class BenchmarkTest   extends FunSpec with ShouldMatchers{

  val testCount = 1000000

  describe("Benchmark test and get"){
    it("JdkReflect") {
      val someObject = new TestBean()
      val method = classOf[TestBean].getMethod("name_$eq", classOf[String])
      for (i <- 0 until 5) {
        val sw = new Stopwatch(true)
        for (j <- 0 until testCount) {
          method.invoke(someObject, "Unmi")
        }
        logger.info(i+"'s "+testCount+" reflect in "+sw)
      }
    }

    it("ReflectAsm") {
      val someObject = new TestBean()
      val access = Mirror.get(classOf[TestBean])
      val idx = access.getIndex("name_$eq")
      for (i <- 0 until 5) {
        val sw = new Stopwatch(true)
        for (j <- 0 until testCount) {
          access.write(someObject, idx,"Unmi")
        }
        logger.info(i+"'s "+testCount+" asm in "+sw)
      }
    }

    it("Direct access") {
      val someObject = new TestBean()
      for (i <- 0 until 5) {
        val sw = new Stopwatch(true)
        for (j <- 0 until testCount) {
          someObject.name="Unmi"
        }
        logger.info(i+"'s "+testCount+" direct in "+sw)
      }
    }
  }
}
