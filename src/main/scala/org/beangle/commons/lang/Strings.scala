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

package org.beangle.commons.lang

import org.beangle.commons.collection.Collections

import java.io.{ByteArrayInputStream, InputStream}
import java.lang.Character.{toLowerCase, isLowerCase as isLower, isUpperCase as isUpper}
import java.nio.charset.Charset
import scala.collection.mutable

/** Operations on String that are `null` safe.
 *
 * @author chaostone 2005-11-15
 * @since 3.0
 */
object Strings {

  /** Constant `DELIMITER=","`
   */
  val DELIMITER = ","

  val Empty = ""

  private val Index_not_found = -1

  /** Capitalizes a String changing the first letter to title case as per
   * `Character# toTitleCase(char)`. No other letters are changed.
   * For a word based algorithm, see returns `null`.
   *
   * {{{
   * capitalize(null)  = null
   * capitalize("")    = ""
   * capitalize("cat") = "Cat"
   * capitalize("cAt") = "CAt"
   * }}}
   *
   * @param str the String to capitalize, may be null
   * @return the capitalized String, `null` if null String input
   * @see #uncapitalize(String)
   * @since 2.0
   */
  def capitalize(str: String): String = {
    if ((str eq null) || str.isEmpty) return str
    val head = str.charAt(0)
    val upper = Character.toUpperCase(head)
    if (upper == head) {
      str
    } else {
      val chars = str.toCharArray
      chars(0) = upper
      new String(chars)
    }
  }

  /** concat.
   *
   * @param seq a String object.
   * @return a String object.
   */
  def concat(seq: Any*): String = join(seq, null)

  /** Checks if CharSequence contains a search CharSequence, handling `null`. This method uses
   * `String#indexOf(String)` if possible.
   * A `null` CharSequence will return `false`.
   *
   * {{{
   * contains(null, *)     = false
   * contains(*, null)     = false
   * contains("", "")      = true
   * contains("abc", "")   = true
   * contains("abc", "a")  = true
   * contains("abc", "z")  = false
   * }}}
   *
   * @param seq       the CharSequence to check, may be null
   * @param searchSeq the CharSequence to find, may be null
   * @return true if the CharSequence contains the search CharSequence,
   *         false if not or `null` string input
   */
  def contains(seq: CharSequence, searchSeq: CharSequence): Boolean = {
    if (seq == null || searchSeq == null) return false
    indexOf(seq, searchSeq, 0) >= 0
  }

  /** Checks if CharSequence contains a search character, handling `null`. This method uses
   * `String#indexOf(int)` if possible.
   * A `null` or empty ("") CharSequence will return `false`.
   *
   * {{{
   * contains(null, *)    = false
   * contains("", *)      = false
   * contains("abc", 'a') = true
   * contains("abc", 'z') = false
   * }}}
   *
   * @param seq        the CharSequence to check, may be null
   * @param searchChar the character to find
   * @return true if the CharSequence contains the search character,
   *         false if not or `null` string input
   * @since 2.0
   */
  def contains(seq: CharSequence, searchChar: Int): Boolean = {
    if (isEmpty(seq)) return false
    indexOf(seq, searchChar, 0) >= 0
  }

  /** count char in host string
   *
   * @param host      String object.
   * @param charactor a char.
   * @return a int.
   */
  def count(host: String, charactor: Char): Int = {
    var count = 0
    for (i <- 0 until host.length if host.charAt(i) == charactor) count += 1
    count
  }

  /** count inner string in host string
   *
   * @param host      a String object.
   * @param searchStr a String object.
   * @return a int.
   */
  def count(host: String, searchStr: String): Int = {
    var count = 0
    var startIndex = host.indexOf(searchStr, 0)
    while (startIndex > -1 && startIndex < host.length) {
      count += 1
      startIndex = host.indexOf(searchStr, startIndex + searchStr.length)
    }
    count
  }

  /** Returns index of searchChar in cs with begin index `start`
   *
   * @param cs         char sequences
   * @param searchChar search char
   * @param start      start position
   */
  private def indexOf(cs: CharSequence, searchChar: CharSequence, start: Int): Int =
    cs.toString.indexOf(searchChar.toString, start)

  /** Finds the first index in the `CharSequence` that matches the specified character.
   *
   * @param cs         the { @code CharSequence} to be processed, not null
   * @param searchChar the char to be searched for
   * @param start      the start index, negative starts at the string start
   * @return the index where the search char was found, -1 if not found
   */
  private def indexOf(cs: CharSequence, searchChar: Int, start: Int): Int = {
    if cs.isInstanceOf[String] then
      cs.asInstanceOf[String].indexOf(searchChar, start)
    else
      ((if (start < 0) 0 else start) until cs.length).find(cs.charAt(_) == searchChar).getOrElse(-1)
  }

  /** insert.
   *
   * @param str a String object.
   * @param c   a String object.
   * @param pos a int.
   * @return a String object.
   */
  def insert(str: String, c: String, pos: Int): String = {
    if (str.length < pos) return str
    if pos < 1 then c + str
    else if pos < str.length then
      str.substring(0, pos) + c + str.substring(pos)
    else str + c
  }

  /** replace [bigen,end] [1...end] with givenStr
   * 可以使用StringBuilder的replace方法替换该方法
   *
   * @param str      a String object.
   * @param beginAt  a int.
   * @param endAt    a int.
   * @param contnets a String object.
   * @return a String object.
   */
  def insert(str: String, contnets: String, beginAt: Int, endAt: Int): String = {
    if (beginAt < 1 || endAt > str.length || endAt < beginAt) return str
    str.substring(0, beginAt - 1) + contnets + str.substring(endAt)
  }

  /** intersectSeq.
   *
   * @param first  a String object.
   * @param second a String object.
   * @return a String object.
   */
  def intersectSeq(first: String, second: String): String = intersectSeq(first, second, DELIMITER)

  /** 返回一个新的逗号相隔字符串，实现其中的单词a-b的功能
   *
   * @param first     a String object.
   * @param second    a String object.
   * @param delimiter a String object.
   * @return a String object.
   */
  def intersectSeq(first: String, second: String, delimiter: String): String = {
    if (isEmpty(first) || isEmpty(second)) return ""
    val rs = Collections.intersection(split(first, ',').toList, split(second, ',').toList)
    val buf = new StringBuilder()
    rs foreach (ele => buf.append(delimiter).append(ele))
    if (buf.nonEmpty) buf.append(delimiter)
    buf.toString
  }

  /** Checks if a CharSequence is whitespace, empty ("") or null.
   *
   * {{{
   * isBlank(null)      = true
   * isBlank("")        = true
   * isBlank(" ")       = true
   * isBlank("bob")     = false
   * isBlank("  bob  ") = false
   * }}}
   *
   * @param cs
   * the CharSequence to check, may be null
   * @return `true` if the CharSequence is null, empty or whitespace
   * @since 3.0
   */
  def isBlank(cs: CharSequence): Boolean = {
    if ((cs eq null) || cs.length == 0) return true
    val strLen = cs.length
    (0 until strLen).forall(i => Character.isWhitespace(cs.charAt(i)))
  }

  /** Returns true is cs is null or cs.length equals 0.
   */
  @inline
  def isEmpty(cs: CharSequence): Boolean = (cs eq null) || 0 == cs.length

  /** isEqualSeq.
   *
   * @param first  not null
   * @param second not null
   * @return a boolean.
   */
  def isEqualSeq(first: String, second: String): Boolean = isEqualSeq(first, second, DELIMITER)

  /** 判断两个","逗号相隔的字符串中的单词是否完全等同.
   *
   * @param first     a String object.
   * @param second    a String object.
   * @param delimiter a String object.
   * @return a boolean.
   */
  def isEqualSeq(first: String, second: String, delimiter: String): Boolean =
    if (isNotEmpty(first) && isNotEmpty(second))
      split(first, delimiter).toSet == split(second, delimiter).toSet
    else
      isEmpty(first) & isEmpty(second)

  /** Checks if a CharSequence is not empty (""), not null and not whitespace only.
   *
   * {{{
   * isNotBlank(null)      = false
   * isNotBlank("")        = false
   * isNotBlank(" ")       = false
   * isNotBlank("bob")     = true
   * isNotBlank("  bob  ") = true
   * }}}
   *
   * @param cs the CharSequence to check, may be null
   * @return `true` if the CharSequence is not empty and not null and not whitespace
   * @since 3.0
   */
  def isNotBlank(cs: CharSequence): Boolean = !isBlank(cs)

  /** Return true if cs not null and cs has length.
   */
  @inline
  def isNotEmpty(cs: CharSequence): Boolean = !(cs eq null) && cs.length > 0

  /** join.
   *
   * @param seq
   * @param delimiter a String object.
   * @return a String object.
   */
  def join(seq: Iterable[_], delimiter: String): String =
    if (null == seq)
      ""
    else {
      val aim = new StringBuilder()
      for (one <- seq) {
        if (null != delimiter && aim.nonEmpty) aim.append(delimiter)
        aim.append(one)
      }
      aim.toString
    }

  /** join.
   *
   * @param seq a String object.
   * @return a String object.
   */
  def join(seq: String*): String = join(seq, DELIMITER)

  /** 将数组中的字符串，用delimiter串接起来.<br>
   * 首尾不加delimiter
   *
   * @param seq       an array of String objects.
   * @param delimiter a String object.
   * @return a String object.
   */
  def join(seq: Array[String], delimiter: String): String =
    if (null == seq)
      ""
    else {
      val seqLen = seq.length
      if (seqLen == 1)
        seq(0)
      else {
        val aim = new StringBuilder()
        seq.indices foreach { i =>
          if (null != delimiter && i > 0) aim.append(delimiter)
          aim.append(seq(i))
        }
        aim.toString
      }
    }

  /** 保持逗号分隔的各个单词都是唯一的。并且按照原来的顺序存放。
   *
   * @param keyString a String object.
   * @return a String object.
   */
  def keepSeqUnique(keyString: String): String = {
    val keyList = split(keyString, ",").toList
    val keys = Collections.newSet[String]
    val newKeys = Collections.newBuffer[String]
    val iter = keyList.iterator
    while (iter.hasNext) {
      val key = iter.next()
      if (!keys.contains(key)) {
        keys.add(key)
        newKeys.addOne(key)
      }
    }
    newKeys.mkString(",")
  }

  /** Left pad a String with a specified character.
   * Pad to a size of `size`.
   *
   * {{{
   * leftPad(null, *, *)     = null
   * leftPad("", 3, 'z')     = "zzz"
   * leftPad("bat", 3, 'z')  = "bat"
   * leftPad("bat", 5, 'z')  = "zzbat"
   * leftPad("bat", 1, 'z')  = "bat"
   * leftPad("bat", -1, 'z') = "bat"
   * }}}
   *
   * @param str     the String to pad out, may be null
   * @param size    the size to pad to
   * @param padChar the character to pad with
   * @return left padded String or original String if no padding is necessary, `null` if null
   *         String input
   * @since 3.0
   */
  def leftPad(str: String, size: Int, padChar: Char): String = {
    if (str == null) return null
    val pads = size - str.length
    if (pads <= 0) return str
    repeat(padChar, pads).concat(str)
  }

  /** Right pad a String with a specified character.
   * The String is padded to the size of `size`.
   *
   * {{{
   * rightPad(null, *, *)     = null
   * rightPad("", 3, 'z')     = "zzz"
   * rightPad("bat", 3, 'z')  = "bat"
   * rightPad("bat", 5, 'z')  = "batzz"
   * rightPad("bat", 1, 'z')  = "bat"
   * rightPad("bat", -1, 'z') = "bat"
   * }}}
   *
   * @param str     the String to pad out, may be null
   * @param size    the size to pad to
   * @param padChar the character to pad with
   * @return right padded String or original String if no padding is necessary, `null` if null
   *         String input
   * @since 3.0
   */
  def rightPad(str: String, size: Int, padChar: Char): String = {
    if (str == null) return null
    val pads = size - str.length
    if (pads <= 0) return str
    str.concat(repeat(padChar, pads))
  }

  /** mergeSeq.
   *
   * @param first  a String object.
   * @param second a String object.
   * @return a String object.
   */
  def mergeSeq(first: String, second: String): String = mergeSeq(first, second, DELIMITER)

  /** 将两个用delimiter串起来的字符串，合并成新的串，重复的"单词"只出现一次.
   * 如果第一个字符串以delimiter开头，第二个字符串以delimiter结尾，<br>
   * 合并后的字符串仍以delimiter开头和结尾.<br>
   * <p>
   * <blockquote>
   *
   * {{{
   * mergeSeq(&quot;,1,2,&quot;, &quot;&quot;) = &quot;,1,2,&quot;;
   * mergeSeq(&quot;,1,2,&quot;, null) = &quot;,1,2,&quot;;
   * mergeSeq(&quot;1,2&quot;, &quot;3&quot;) = &quot;1,2,3&quot;;
   * mergeSeq(&quot;1,2&quot;, &quot;3,&quot;) = &quot;1,2,3,&quot;;
   * mergeSeq(&quot;,1,2&quot;, &quot;3,&quot;) = &quot;,1,2,3,&quot;;
   * mergeSeq(&quot;,1,2,&quot;, &quot;,3,&quot;) = &quot;,1,2,3,&quot;;
   * }}}
   *
   * </blockquote>
   *
   * @param first     a String object.
   * @param second    a String object.
   * @param delimiter a String object.
   * @return a String object.
   */
  def mergeSeq(first: String, second: String, delimiter: String): String = {
    if (isNotEmpty(second) && isNotEmpty(first)) {
      val firstSeq = split(first, delimiter).toList
      val secondSeq = split(second, delimiter).toList
      val rs = Collections.union(firstSeq, secondSeq)
      val buf = new StringBuilder()
      for (ele <- rs) buf.append(delimiter).append(ele)
      if (buf.nonEmpty) buf.append(delimiter)
      buf.toString
    } else
      (if ((first == null)) "" else first) + (if ((second == null)) "" else second)
  }

  /** removeWord.
   *
   * @param host a String object.
   * @param word a String object.
   * @return a String object.
   */
  def removeWord(host: String, word: String): String = removeWord(host, word, DELIMITER)

  /** removeWord.
   *
   * @param host      a String object.
   * @param word      a String object.
   * @param delimiter a String object.
   * @return a String object.
   */
  def removeWord(host: String, word: String, delimiter: String): String = {
    if host.indexOf(word) == -1 then host
    else
      split(host, delimiter).filterNot(_ == word).mkString(delimiter)
  }

  /** Returns padding using the specified delimiter repeated to a given length.
   *
   * {{{
   * repeat(0, 'e')  = ""
   * repeat(3, 'e')  = "eee"
   * repeat(-2, 'e') = ""
   * }}}
   *
   * @param ch     character to repeat
   * @param repeat number of times to repeat char, negative treated as zero
   * @return String with repeated character
   * @see #repeat(String, int)
   */
  def repeat(ch: Char, repeat: Int): String = {
    if (repeat <= 1) {
      return if repeat <= 0 then "" else ch.toString
    }
    val buf = new Array[Char](repeat)
    var i = repeat - 1
    while (i >= 0) {
      buf(i) = ch
      i -= 1
    }
    new String(buf)
  }

  /** Repeat a String `repeat` times to form a new String.
   *
   * {{{
   * repeat(null, 2) = null
   * repeat("", 0)   = ""
   * repeat("", 2)   = ""
   * repeat("a", 3)  = "aaa"
   * repeat("ab", 2) = "abab"
   * repeat("a", -2) = ""
   * }}}
   *
   * @param str    the String to repeat, may be null
   * @param repeat number of times to repeat str, negative treated as zero
   * @return a new String consisting of the original String repeated, `null` if null String
   *         input
   * @since 3.0
   */
  def repeat(str: String, repeat: Int): String = {
    if (str == null) return null
    if (repeat <= 1) {
      return if (repeat <= 0) "" else str
    }
    val len = str.length
    val longSize = len.toLong * repeat.toLong
    val size = longSize.toInt
    if (size != longSize)
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + String.valueOf(longSize))
    val array = new Array[Char](size)
    str.getChars(0, len, array, 0)
    var n = len
    while (n < size - n) {
      System.arraycopy(array, 0, array, n, n)
      n <<= 1
    }
    System.arraycopy(array, 0, array, n, size - n)
    new String(array)
  }

  /** Replaces all occurrences of a String within another String.
   * A `null` reference passed to this method is a no-op.
   *
   * {{{
   * replace(null, *, *)        = null
   * replace("", *, *)          = ""
   * replace("any", null, *)    = "any"
   * replace("any", *, null)    = "any"
   * replace("any", "", *)      = "any"
   * replace("aba", "a", null)  = "aba"
   * replace("aba", "a", "")    = "b"
   * replace("aba", "a", "z")   = "zbz"
   * }}}
   *
   * @param text         text to search and replace in, may be null
   * @param searchString the String to search for, may be null
   * @param replacement  the String to replace it with, may be null
   * @return the text with any replacements processed, `null` if null String input
   */
  def replace(text: String, searchString: String, replacement: String): String = {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null)
      return text
    var start = 0
    var end = text.indexOf(searchString, start)
    if (end == -1) return text
    val replLength = searchString.length
    var increase = replacement.length - replLength
    increase = if (increase < 0) 0 else increase
    increase *= 16
    val buf = new StringBuilder(text.length + increase)
    while (end != -1) {
      buf.append(text.substring(start, end)).append(replacement)
      start = end + replLength
      end = text.indexOf(searchString, start)
    }
    buf.append(text.substring(start))
    buf.toString
  }

  /** split.
   *
   * @param target a String object.
   * @return an array of String objects.
   */
  def split(target: String): Array[String] = {
    split(target, Array(',', ';', '\r', '\n', ' '))
  }

  /** Splits the provided text into an array, separator specified. This is an alternative to using
   * StringTokenizer.
   * A `null` input String returns `null`.
   *
   * {{{
   * split(null, *)         = null
   * split("", *)           = []
   * split("a.b.c", '.')    = ["a", "b", "c"]
   * split("a..b.c", '.')   = ["a", "b", "c"]
   * split("a:b:c", '.')    = ["a:b:c"]
   * split("a b c", ' ')    = ["a", "b", "c"]
   * }}}
   */
  def split(str: String, separatorChar: Char): Array[String] = {
    if (str == null) return null
    val len = str.length
    if (len == 0) return new Array[String](0)
    val list = new mutable.ListBuffer[String]
    var i = 0
    var start = 0
    val length = str.length
    val chars = new Array[Char](length)
    str.getChars(0, length, chars, 0)
    while (i < len) {
      if (chars(i) == separatorChar) {
        //ignore continue separator
        if (start < i) addNonEmpty(list, chars, start, i)
        start = i + 1
      }
      i += 1
    }
    if (start < i) addNonEmpty(list, chars, start, i)
    list.toArray
  }

  def addNonEmpty(buffer: mutable.Buffer[String], chars: Array[Char], start: Int, end: Int): Unit = {
    val rs = new String(chars, start, end - start).trim()
    if rs.nonEmpty then buffer.addOne(rs)
  }

  /** split with separators
   *
   * @param target         a String object.
   * @param separatorChars an array of char.
   * @return an array of String objects.
   */
  def split(target: String, separatorChars: Array[Char]): Array[String] = {
    if (null == target) return new Array[String](0)

    if (separatorChars.length == 1) split(target, separatorChars(0))
    else {
      val first = separatorChars(0)
      val all = separatorChars.toSet
      val sb = target.toCharArray
      for (i <- 0 until sb.length if all.contains(sb(i))) sb(i) = first
      split(new String(sb), first)
    }
  }

  /** Splits the provided text into an array, separators specified. This is an alternative to using
   * StringTokenizer.
   * A `null` input String returns `null`. A `null` separatorChars splits on
   * whitespace.
   *
   * {{{
   * split(null, *)         = null
   * split("", *)           = []
   * split("abc def", null) = ["abc", "def"]
   * split("abc def", " ")  = ["abc", "def"]
   * split("abc  def", " ") = ["abc", "def"]
   * split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
   * }}}
   */
  def split(str: String, separatorChars: String): Array[String] = {
    if (str == null) return null
    val len = str.length
    if (len == 0) return new Array[String](0)
    val list = new mutable.ListBuffer[String]
    var i, start = 0
    var matched = false
    val sepChars = if (null == separatorChars) " " else separatorChars
    while (i < len) {
      if (sepChars.indexOf(str.charAt(i)) >= 0) {
        if (matched) {
          list += str.substring(start, i)
          matched = false
        }
        start = i + 1
      } else
        matched = true
      i += 1
    }
    if (matched) list += str.substring(start, i)
    list.toArray
  }

  /** 将1-2,3,4-9之类的序列拆分成数组
   *
   * @param numSeq a String object.
   * @return an array of Int objects.
   */
  def splitNumSeq(numSeq: String): Array[Int] = {
    if (isEmpty(numSeq)) return null
    val numArray = split(numSeq, ',')
    val numSet = new mutable.HashSet[Int]
    numArray.indices foreach { i =>
      val num = numArray(i)
      if (num.contains("-")) {
        val termFromTo = split(num, '-')
        val from = Numbers.toInt(termFromTo(0))
        val to = Numbers.toInt(termFromTo(1))
        var j = from
        while (j <= to) {
          numSet.add(j)
          j += 1
        }
      } else
        numSet.add(Numbers.toInt(num))
    }
    numSet.toArray
  }

  /** splitToInteger.
   */
  def splitToInt(ids: String): Seq[Int] =
    if (isEmpty(ids)) List.empty else Numbers.toInt(split(ids, ',')).toIndexedSeq

  /** Split string to long seq */
  def splitToLong(ids: String): Seq[Long] = {
    if (isEmpty(ids)) List.empty else Numbers.toLong(split(ids, ',')).toIndexedSeq
  }

  /** Gets a substring from the specified String avoiding exceptions.
   *
   * A negative start position can be used to start/end `n` characters from the end of the
   * String.
   * The returned substring starts with the character in the `start` position and ends before
   * the `end` position. All position counting is zero-based -- i.e., to start at the
   * beginning of the string use `start = 0`. Negative start and end positions can be used to
   * specify offsets relative to the end of the String.
   * If `start` is not strictly to the left of `end`, "" is returned.
   *
   * {{{
   * substring(null, *, *)    = null
   * substring("", * ,  *)    = "";
   * substring("abc", 0, 2)   = "ab"
   * substring("abc", 2, 0)   = ""
   * substring("abc", 2, 4)   = "c"
   * substring("abc", 4, 6)   = ""
   * substring("abc", 2, 2)   = ""
   * substring("abc", -2, -1) = "b"
   * substring("abc", -4, 2)  = "ab"
   * }}}
   *
   * @param str        the String to get the substring from, may be null
   * @param startIndex the position to start from, negative means
   *                   count back from the end of the String by this many characters
   * @param endIndex   the position to end at (exclusive), negative means
   *                   count back from the end of the String by this many characters
   * @return substring from start position to end position, `null` if null String input
   */
  def substring(str: String, startIndex: Int, endIndex: Int): String = {
    if (str == null) return null
    var start = startIndex
    var end = endIndex

    if (start < 0) start = str.length + start

    if (end < 0) end = str.length + end
    if (end > str.length) end = str.length

    if (start > end) return ""
    if (start < 0) start = 0
    if (end < 0) end = 0
    str.substring(start, end)
  }

  /** subtractSeq.
   *
   * @param first  a String object.
   * @param second a String object.
   * @return a String object.
   */
  def subtractSeq(first: String, second: String): String = subtractSeq(first, second, DELIMITER)

  /** 返回一个新的逗号相隔字符串，实现其中的单词a-b的功能. 新的字符串将以,开始,结束<br>
   */
  def subtractSeq(first: String, second: String, delimiter: String): String = {
    if (isEmpty(first)) return ""
    if (isEmpty(second)) {
      val builder = new StringBuilder()
      if (!first.startsWith(delimiter)) builder.append(delimiter).append(first)
      if (!first.endsWith(delimiter)) builder.append(first).append(delimiter)
      return builder.toString
    }
    val firstSeq = split(first, delimiter).toList
    val secondSeq = split(second, delimiter).toList
    val rs = Collections.subtract(firstSeq, secondSeq)
    val buf = new StringBuilder()
    rs foreach { ele => buf.append(delimiter).append(ele) }
    if (buf.nonEmpty) buf.append(delimiter)
    buf.toString
  }

  /** unCamel.
   */
  def unCamel(str: String): String = unCamel(str, '-', true)

  /** unCamel.
   *
   * @param str       a String object.
   * @param seperator a char.
   * @return a String object.
   */
  def unCamel(str: String, seperator: Char): String = unCamel(str, seperator, true)

  /** 将驼峰表示法转换为下划线小写表示
   *
   * @param str       a String object.
   * @param seperator a char.
   * @param lowercase a boolean.
   * @return a String object.
   */
  def unCamel(str: String, seperator: Char, lowercase: Boolean): String = {
    if (3 > str.length) return if (lowercase) str.toLowerCase else str
    val ca = str.toCharArray
    val build = new StringBuilder(ca.length + 5)
    build.append(if (lowercase) toLowerCase(ca(0)) else ca(0))
    var lower1 = isLower(ca(0))
    var i = 1
    while (i < ca.length - 1) {
      val cur = ca(i)
      val next = ca(i + 1)
      val upper2 = isUpper(cur)
      val lower3 = isLower(next)
      if (lower1 && upper2 && lower3) {
        build.append(seperator)
        build.append(if (lowercase) toLowerCase(cur) else cur)
        build.append(next)
        i += 2
      } else {
        if (lowercase && upper2) build.append(toLowerCase(cur))
        else build.append(cur)
        lower1 = !upper2
        i += 1
      }
    }
    if (i == ca.length - 1) {
      if (isLower(ca(i - 1)) && isUpper(ca(i))) build.append(seperator)
      build.append(if (lowercase) toLowerCase(ca(i)) else ca(i))
    }
    build.toString
  }

  /** Gets the substring before the first occurrence of a separator. The separator is not returned.
   * A `null` string input will return `null`. An empty ("") string input will return
   * the empty string. A `null` separator will return the input string.
   * If nothing is found, the string input is returned.
   *
   * {{{
   * substringBefore(null, *)      = null
   * substringBefore("", *)        = ""
   * substringBefore("abc", "a")   = ""
   * substringBefore("abcba", "b") = "a"
   * substringBefore("abc", "c")   = "ab"
   * substringBefore("abc", "d")   = "abc"
   * substringBefore("abc", "")    = ""
   * substringBefore("abc", null)  = "abc"
   * }}}
   *
   * @param str       the String to get a substring from, may be null
   * @param separator the String to search for, may be null
   * @return the substring before the first occurrence of the separator, `null` if null String
   *         input
   * @since 2.0
   */
  def substringBefore(str: String, separator: String): String = {
    if (isEmpty(str) || separator == null) return str
    if (separator.isEmpty) return Empty
    val pos = str.indexOf(separator)
    if (pos == Index_not_found) return str

    str.substring(0, pos)
  }

  /** Gets the substring after the first occurrence of a separator. The separator is not returned.
   * A `null` string input will return `null`. An empty ("") string input will return
   * the empty string. A `null` separator will return the empty string if the input string is
   * not `null`.
   * If nothing is found, the empty string is returned.
   *
   * {{{
   * substringAfter(null, *)      = null
   * substringAfter("", *)        = ""
   * substringAfter(*, null)      = ""
   * substringAfter("abc", "a")   = "bc"
   * substringAfter("abcba", "b") = "cba"
   * substringAfter("abc", "c")   = ""
   * substringAfter("abc", "d")   = ""
   * substringAfter("abc", "")    = "abc"
   * }}}
   *
   * @param str       the String to get a substring from, may be null
   * @param separator the String to search for, may be null
   * @return the substring after the first occurrence of the separator, `null` if null String
   *         input
   * @since 2.0
   */
  def substringAfter(str: String, separator: String): String = {
    if (isEmpty(str)) return str
    if (separator == null) return Empty
    val pos = str.indexOf(separator)
    if (pos == Index_not_found) return Empty
    str.substring(pos + separator.length)
  }

  /** Gets the String that is nested in between two Strings. Only the first match is returned.
   * A `null` input String returns `null`. A `null` open/close returns
   * `null` (no match). An empty ("") open and close returns an empty string.
   *
   * {{{
   * substringBetween("wx[b]yz", "[", "]") = "b"
   * substringBetween(null, *, *)          = null
   * substringBetween(*, null, *)          = null
   * substringBetween(*, *, null)          = null
   * substringBetween("", "", "")          = ""
   * substringBetween("", "", "]")         = null
   * substringBetween("", "[", "]")        = null
   * substringBetween("yabcz", "", "")     = ""
   * substringBetween("yabcz", "y", "z")   = "abc"
   * substringBetween("yabczyabcz", "y", "z")   = "abc"
   * }}}
   *
   * @param str   the String containing the substring, may be null
   * @param open  the String before the substring, may be null
   * @param close the String after the substring, may be null
   * @return the substring, `null` if no match
   * @since 3.0
   */
  def substringBetween(str: String, open: String, close: String): String = {
    if (str == null || open == null || close == null) return null
    val start = str.indexOf(open)
    if (start != Index_not_found) {
      val end = str.indexOf(close, start + open.length)
      if (end != Index_not_found) return str.substring(start + open.length, end)
    }
    null
  }

  /** Gets the substring before the last occurrence of a separator. The separator is not returned.
   * A `null` string input will return `null`. An empty ("") string input will return
   * the empty string. An empty or `null` separator will return the input string.
   * If nothing is found, the string input is returned.
   *
   * {{{
   * substringBeforeLast(null, *)      = null
   * substringBeforeLast("", *)        = ""
   * substringBeforeLast("abcba", "b") = "abc"
   * substringBeforeLast("abc", "c")   = "ab"
   * substringBeforeLast("a", "a")     = ""
   * substringBeforeLast("a", "z")     = "a"
   * substringBeforeLast("a", null)    = "a"
   * substringBeforeLast("a", "")      = "a"
   * }}}
   *
   * @param str       the String to get a substring from, may be null
   * @param separator the String to search for, may be null
   * @return the substring before the last occurrence of the separator, `null` if null String
   *         input
   * @since 3.0
   */
  def substringBeforeLast(str: String, separator: String): String = {
    if (isEmpty(str) || isEmpty(separator)) return str
    val pos = str.lastIndexOf(separator)
    if (pos == Index_not_found) return str
    str.substring(0, pos)
  }

  /** Gets the substring after the last occurrence of a separator. The separator is not returned.
   * A `null` string input will return `null`. An empty ("") string input will return
   * the empty string. An empty or `null` separator will return the empty string if the input
   * string is not `null`.
   * If nothing is found, the empty string is returned.
   *
   * {{{
   * substringAfterLast(null, *)      = null
   * substringAfterLast("", *)        = ""
   * substringAfterLast(*, "")        = ""
   * substringAfterLast(*, null)      = ""
   * substringAfterLast("abc", "a")   = "bc"
   * substringAfterLast("abcba", "b") = "a"
   * substringAfterLast("abc", "c")   = ""
   * substringAfterLast("a", "a")     = ""
   * substringAfterLast("a", "z")     = ""
   * }}}
   *
   * @param str       the String to get a substring from, may be null
   * @param separator the String to search for, may be null
   * @return the substring after the last occurrence of the separator, `null` if null String
   *         input
   * @since 3.0
   */
  def substringAfterLast(str: String, separator: String): String = {
    if (isEmpty(str)) return str
    if (isEmpty(separator)) return Empty
    val pos = str.lastIndexOf(separator)
    if (pos == Index_not_found || pos == str.length - separator.length) return Empty
    str.substring(pos + separator.length)
  }

  /** Removes control characters (char &lt;= 32) from both ends of this String, handling `null`
   * by returning `null`.
   * The String is trimmed using `String#trim()`. Trim removes start and end characters &lt;=
   * 32.
   *
   * {{{
   * trim(null)          = null
   * trim("")            = ""
   * trim("     ")       = ""
   * trim("abc")         = "abc"
   * trim("    abc    ") = "abc"
   * }}}
   *
   * @param str the String to be trimmed, may be null
   * @return the trimmed string, `null` if null String input
   * @since 3.0
   */
  def trim(str: String): String =
    if (str == null) null else str.trim()

  def trimEnd(str: String): String =
    if (str == null)
      null
    else {
      var len = str.length
      while (len > 0 && (str.charAt(len - 1) <= ' '))
        len -= 1
      if (len < str.length) str.substring(0, len) else str
    }

  /** Strips any of a set of characters from the end of a String.
   *
   * <p>A `null` input String returns `null`.
   * An empty string ("") input returns the empty string.</p>
   *
   * <p>If the stripChars String is `null`, whitespace is
   * stripped as defined by `Character#isWhitespace(char)`.</p>
   *
   * {{{
   * stripEnd(null, *)          = null
   * stripEnd("", *)            = ""
   * stripEnd("abc", "")        = "abc"
   * stripEnd(*, null)    = *
   * stripEnd("  abcyx", "xyz") = "  abc"
   * }}}
   *
   * @param str        the String to remove characters from, may be null
   * @param stripChars the characters to remove, null treated as whitespace
   * @return the stripped String, `null` if null String input
   */
  def stripEnd(str: String, stripChars: String): String =
    if (str == null || str.isEmpty || null == stripChars || stripChars.isEmpty)
      str
    else {
      var end = str.length
      while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1))
        end -= 1
      str.substring(0, end)
    }

  /** Uncapitalizes a String changing the first letter to title case as per
   * [[java.lang.Character#toLowerCase(char)]]. No other letters are changed.
   * For a word based algorithm, see String returns `null`.
   *
   * {{{
   * uncapitalize(null)  = null
   * uncapitalize("")    = ""
   * uncapitalize("Cat") = "cat"
   * uncapitalize("CAT") = "cAT"
   * }}}
   *
   * @param str the String to uncapitalize, may be null
   * @return the uncapitalized String, `null` if null String input
   * @see #capitalize(String)
   * @since 3.0
   */
  def uncapitalize(str: String): String = {
    if ((str eq null) || str.isEmpty) return str
    val head = str.charAt(0)
    val lower = Character.toLowerCase(head)
    if (lower == head)
      str
    else {
      val chars = str.toCharArray
      chars(0) = lower
      new String(chars)
    }
  }

  /** Returns either the passed in CharSequence, or if the CharSequence is whitespace, empty ("") or
   * `null`, the value of `defaultStr`.
   *
   * @param str
   * @param defaultStr
   */
  def defaultIfBlank[T <: CharSequence](str: T, defaultStr: T): T =
    if (isBlank(str)) defaultStr else str

  def lowerCase(s: String): String =
    if (null == s) null else s.toLowerCase()

  def upperCase(s: String): String =
    if (null == s) null else s.toUpperCase()

  def abbreviate(str: String, maxWidth: Int): String =
    abbreviate(str, 0, maxWidth);

  def abbreviate(str: String, offset: Int, maxWidth: Int): String = {
    if (str eq null) return null
    if (maxWidth < 4) throw new IllegalArgumentException("Minimum abbreviation width is 4")
    if (str.length <= maxWidth) return str
    var newoffset = offset
    if (newoffset > str.length()) newoffset = str.length
    if (str.length() - newoffset < maxWidth - 3) newoffset = str.length() - (maxWidth - 3)
    val abrevMarker = "..."
    if (newoffset <= 4) return str.substring(0, maxWidth - 3) + abrevMarker
    if (maxWidth < 7) throw new IllegalArgumentException("Minimum abbreviation width with offset is 7")

    if (newoffset + maxWidth - 3 < str.length)
      abrevMarker + abbreviate(str.substring(newoffset), maxWidth - 3)
    else
      abrevMarker + str.substring(str.length() - (maxWidth - 3))
  }

  /** Removes all occurrences of a character from within the source string.
   * A `null` source string will return `null`. An empty ("") source string will return
   * the empty string.
   *
   * {{{
   * StringUtils.remove(null, *) = null
   * StringUtils.remove("", *) = ""
   * StringUtils.remove("queued", 'u') = "qeed"
   * StringUtils.remove("queued", 'z') = "queued"
   * }}}
   *
   * @param str    the source String to search, may be null
   * @param remove the char to search for and remove, may be null
   * @return the substring with the char removed if found, `null` if null String input
   */
  def remove(str: String, remove: Char): String = {
    if (isEmpty(str) || str.indexOf(remove) == -1)
      return str
    val chars = str.toCharArray
    var i, pos = 0
    while (i < chars.length) {
      if (chars(i) != remove) {
        chars(pos) = chars(i)
        pos += 1
      }
      i += 1
    }
    new String(chars, 0, pos);
  }

  def format(format: String, args: Any*): String = {
    new java.util.Formatter().format(format, args.toArray.asInstanceOf[Array[Object]]: _*).toString
  }

  def asStream(contents: String): InputStream = {
    new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8))
  }

  def asStream(contents: String, charset: Charset): InputStream = {
    new ByteArrayInputStream(contents.getBytes(charset))
  }
}
