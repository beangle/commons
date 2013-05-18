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
package org.beangle.commons.lang

import org.beangle.commons.lang.Strings._
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
//remove if not needed
import scala.collection.JavaConversions._

@Test
class StringsTest {

  def testCount() {
    val targetStr = "11001101101111"
    val searchStr = "11"
    assertEquals(count(targetStr, searchStr), 5)
  }

  def testUnCamel() {
    assertEquals(unCamel("MyCountInI_cbc", '-'), "my-count-ini_cbc")
    assertEquals(unCamel("MyCounT", '_'), "my_count")
    assertEquals(unCamel("MYCOUNT", '-'), "mycount")
    assertEquals(unCamel("parent_id", '_'), "parent_id")
    assertEquals(unCamel("parentId", '_'), "parent_id")
  }

  def testSplit2() {
    val target = " abc ,; def ,;; ghi\r\n opq"
    val codes = split(target)
    assertEquals(codes.length, 4)
    assertEquals(codes(3), "opq")
  }

  def testIsEqualSeq() {
    val first = "123,4546,"
    val second = ",4546,123"
    assertTrue(isEqualSeq(first, second))
    assertTrue(isEqualSeq(first, second, ","))
  }

  def testMergeSeq() {
    val first = ",1,2,"
    val second = "3,"
    val third = ""
    val forth: String = null
    assertTrue(isEqualSeq(mergeSeq(first, second), ",1,2,3,"))
    assertTrue(isEqualSeq(mergeSeq(first, second), ",1,2,3,"))
    assertTrue(isEqualSeq(mergeSeq(first, third), ",1,2,"))
    assertTrue(isEqualSeq(mergeSeq(first, forth), ",1,2,"))
  }

  def testSplitNumSeq() {
    val a = "1-2,3,5-9,3,"
    val nums = splitNumSeq(a)
    assertEquals(nums.length, 8)
  }

  def testMisc() {
    assertEquals(",2,", subtractSeq("1,2", "1"))
    assertFalse(isEqualSeq(",2005-9,", ",2005-9,2006-9,"))
  }

  def testRepeat() {
    assertEquals("", repeat("ad", 0))
    assertEquals("adadad", repeat("ad", 3))
  }

  def testSplit() {
    assertEquals(Array("a", "b", "c"), split("a.b.c.", '.'))
    assertEquals(Array("a", "b", "c"), split(".a..b.c", '.'))
    assertEquals(Array("a:b:c"), split("a:b:c", '.'))
    assertEquals(Array(), split("", null))
    assertEquals(Array("abc", "def"), split("abc def", null))
    assertEquals(Array("abc", "def"), split("abc def", " "))
    assertEquals(Array("ab", "cd", "ef"), split("ab:cd:ef", ":"))
  }

  def testReplace() {
    assertEquals(replace(null, "x", null), null)
    assertEquals(replace("", "dd", "xx"), "")
    assertEquals(replace("any", null, "xx"), "any")
    assertEquals(replace("any", "d", null), "any")
    assertEquals(replace("any", "", "dd"), "any")
    assertEquals(replace("aba", "a", null), "aba")
    assertEquals(replace("aba", "a", ""), "b")
    assertEquals(replace("aba", "a", "z"), "zbz")
  }
}
