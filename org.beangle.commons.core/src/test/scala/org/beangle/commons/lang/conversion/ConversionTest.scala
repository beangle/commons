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
package org.beangle.commons.lang.conversion

import org.beangle.commons.lang.conversion.impl.DefaultConversion
import org.testng.Assert
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class ConversionTest {

  def testConvert() {
    val con = new DefaultConversion()
    con.convert(2.5f, classOf[Integer])
  }

  def testConvertArray() {
    val con = new DefaultConversion()
    con.convert(Array("2", "3"), classOf[Array[Integer]])
  }

  def testConvertPrimitive() {
    val con = new DefaultConversion()
    con.convert("2", classOf[Int]).toInt
    con.convert(3, classOf[Integer])
  }

  def testConvertPrimitiveArray() {
    val con = new DefaultConversion()
    con.convert(Array("2", "3.4"), classOf[Array[Float]]).asInstanceOf[Array[Float]]
  }
}
