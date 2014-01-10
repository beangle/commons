/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import org.beangle.commons.lang.Strings._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class StringsTest extends FunSpec with ShouldMatchers {

  describe("Strings") {
    it("Count target string") {
      count("11001101101111", "11") should be(5)
    }

    it("UnCamel words") {
      unCamel("MyCountInI_cbc", '-') should equal("my-count-ini_cbc")
      unCamel("MyCounT", '_') should equal("my_coun_t")
      unCamel("MYCOUNT", '-') should equal("mycount")
      unCamel("parent_id", '_') should equal("parent_id")
      unCamel("parentId", '_') should equal("parent_id")
      unCamel("scoreA", '_') should equal("score_a")
    }

    it("Split with multi separator") {
      val target = " abc ,; def ,;; ghi\r\n opq"
      val codes = split(target)
      codes.length should be(4)
      codes(3) should equal("opq")
    }

    it("IsEqualSeq compare different string seq") {
      val first = "123,4546,"
      val second = ",4546,123"
      isEqualSeq(first, second) should be(true)
      isEqualSeq(first, second, ",") should be(true)
    }

    it("MergeSeq merge to seprated seq by ,") {
      val first = ",1,2,"
      val second = "3,"
      val third = ""
      val forth: String = null
      isEqualSeq(mergeSeq(first, second), ",1,2,3,") should be(true)
      isEqualSeq(mergeSeq(first, second), ",1,2,3,") should be(true)
      isEqualSeq(mergeSeq(first, third), ",1,2,") should be(true)
      isEqualSeq(mergeSeq(first, forth), ",1,2,") should be(true)
    }

    it("SplitNum int array") {
      val a = "1-2,3,5-9,3,"
      val nums = splitNumSeq(a)
      nums.length should be(8)
    }

    it("Misc") {
      subtractSeq("1,2", "1") should equal(",2,")
      isEqualSeq(",2005-9,", ",2005-9,2006-9,") should be(false)
    }

    it("Repeat string zero or greater") {
      repeat("ad", 0) should equal("")
      repeat("ad", 3) should equal("adadad")
    }

    it("Split into array") {
      split("a.b.c.", '.') should equal(Array("a", "b", "c"))
      split(".a..b.c", '.') should equal(Array("a", "b", "c"))
      split("a:b:c", '.') should equal(Array("a:b:c"))
      split("", null.asInstanceOf[String]) should equal(Array())
      split("abc def", null.asInstanceOf[String]) should equal(Array("abc", "def"))
      split("abc def", " ") should equal(Array("abc", "def"))
      split("ab:cd:ef", ":") should equal(Array("ab", "cd", "ef"))
    }

    it("Replace target with null or string") {
      replace(null, "x", null) should be(null)
      replace("", "dd", "xx") should equal("")
      replace("any", null, "xx") should equal("any")
      replace("any", "d", null) should equal("any")
      replace("any", "", "dd") should equal("any")
      replace("aba", "a", null) should equal("aba")
      replace("aba", "a", "") should equal("b")
      replace("aba", "a", "z") should equal("zbz")
    }
  }
}
