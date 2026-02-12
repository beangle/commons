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

  /** Constant `DELIMITER=","`. */
  val DELIMITER = ","

  /** Empty string constant. */
  val Empty = ""

  private val Index_not_found = -1

  /** Capitalizes a String changing the first letter to title case as per
   * `Character#toTitleCase(char)`. No other letters are changed. Returns `null` for null input.
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

  /** Concatenates the given sequence of values into a single string using null as delimiter.
   *
   * @param seq the sequence of values to concatenate
   * @return the concatenated string
   */
  def concat(seq: Any*): String = join(seq, null)

  /** Checks if CharSequence contains a search CharSequence, handling `null`.
   * Uses `String#indexOf(String)` if possible. A `null` CharSequence returns `false`.
   * {{{
   * contains(null, *)     = false
   * contains(*, null)     = false
   * contains("", "")      = true
   * contains("abc", "a")  = true
   * contains("abc", "z")  = false
   * }}}
   *
   * @param seq       the CharSequence to check, may be null
   * @param searchSeq the CharSequence to find, may be null
   * @return true if seq contains searchSeq, false otherwise
   */
  def contains(seq: CharSequence, searchSeq: CharSequence): Boolean = {
    if (seq == null || searchSeq == null) return false
    indexOf(seq, searchSeq, 0) >= 0
  }

  /** Checks if CharSequence contains a search character, handling `null`.
   * Uses `String#indexOf(int)` if possible. Null or empty CharSequence returns `false`.
   * {{{
   * contains(null, *)    = false
   * contains("", *)      = false
   * contains("abc", 'a') = true
   * contains("abc", 'z') = false
   * }}}
   *
   * @param seq        the CharSequence to check, may be null
   * @param searchChar the character to find
   * @return true if seq contains searchChar, false otherwise
   * @since 2.0
   */
  def contains(seq: CharSequence, searchChar: Int): Boolean = {
    if (isEmpty(seq)) return false
    indexOf(seq, searchChar, 0) >= 0
  }

  /** Counts the number of occurrences of a character in the host string.
   *
   * @param host      the string to search in
   * @param charactor the character to count
   * @return the count of occurrences
   */
  def count(host: String, charactor: Char): Int = {
    var count = 0
    for (i <- 0 until host.length if host.charAt(i) == charactor) count += 1
    count
  }

  /** Counts the number of occurrences of a substring in the host string.
   *
   * @param host      the string to search in
   * @param searchStr the substring to count
   * @return the count of occurrences
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

  private def indexOf(cs: CharSequence, searchChar: CharSequence, start: Int): Int =
    cs.toString.indexOf(searchChar.toString, start)

  private def indexOf(cs: CharSequence, searchChar: Int, start: Int): Int = {
    if cs.isInstanceOf[String] then
      cs.asInstanceOf[String].indexOf(searchChar, start)
    else
      ((if (start < 0) 0 else start) until cs.length).find(cs.charAt(_) == searchChar).getOrElse(-1)
  }

  /** Inserts a string at the specified position.
   *
   * @param str the original string
   * @param c   the string to insert
   * @param pos the insertion position (1-based)
   * @return the result string with the inserted content
   */
  def insert(str: String, c: String, pos: Int): String = {
    if (str.length < pos) return str
    if pos < 1 then c + str
    else if pos < str.length then
      str.substring(0, pos) + c + str.substring(pos)
    else str + c
  }

  /** Replaces the substring from beginAt to endAt (1-based inclusive) with the given string.
   *
   * @param str      the original string
   * @param contnets the replacement string
   * @param beginAt  the start position (1-based)
   * @param endAt    the end position (1-based, inclusive)
   * @return the result string with the replaced content
   */
  def insert(str: String, contnets: String, beginAt: Int, endAt: Int): String = {
    if (beginAt < 1 || endAt > str.length || endAt < beginAt) return str
    str.substring(0, beginAt - 1) + contnets + str.substring(endAt)
  }

  /** Returns a new comma-separated string containing words that appear in both input strings.
   *
   * @param first  the first delimiter-separated string
   * @param second the second delimiter-separated string
   * @return the intersection of words from both strings, comma-separated
   */
  def intersectSeq(first: String, second: String): String = intersectSeq(first, second, DELIMITER)

  /** Returns a new delimiter-separated string containing words that appear in both input strings.
   *
   * @param first     the first delimiter-separated string
   * @param second    the second delimiter-separated string
   * @param delimiter the delimiter used to separate words
   * @return the intersection of words from both strings
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
   * {{{
   * isBlank(null)      = true
   * isBlank("")        = true
   * isBlank(" ")       = true
   * isBlank("bob")     = false
   * isBlank("  bob  ") = false
   * }}}
   *
   * @param cs the CharSequence to check, may be null
   * @return true if cs is null, empty or whitespace only
   * @since 3.0
   */
  def isBlank(cs: CharSequence): Boolean = {
    if ((cs eq null) || cs.length == 0) return true
    val strLen = cs.length
    (0 until strLen).forall(i => Character.isWhitespace(cs.charAt(i)))
  }

  /** Returns true if the CharSequence is null or has zero length. */
  @inline
  def isEmpty(cs: CharSequence): Boolean = (cs eq null) || 0 == cs.length

  /** Checks whether two comma-separated strings contain exactly the same set of words.
   *
   * @param first  the first string to compare
   * @param second the second string to compare
   * @return true if both contain the same words (order independent)
   */
  def isEqualSeq(first: String, second: String): Boolean = isEqualSeq(first, second, DELIMITER)

  /** Checks whether two delimiter-separated strings contain exactly the same set of words.
   *
   * @param first     the first string to compare
   * @param second    the second string to compare
   * @param delimiter the delimiter used to separate words
   * @return true if both contain the same words (order independent)
   */
  def isEqualSeq(first: String, second: String, delimiter: String): Boolean =
    if (isNotEmpty(first) && isNotEmpty(second))
      split(first, delimiter).toSet == split(second, delimiter).toSet
    else
      isEmpty(first) & isEmpty(second)

  /** Checks if a CharSequence is not empty, not null, and not whitespace only.
   * {{{
   * isNotBlank(null)      = false
   * isNotBlank("")        = false
   * isNotBlank(" ")       = false
   * isNotBlank("bob")     = true
   * isNotBlank("  bob  ") = true
   * }}}
   *
   * @param cs the CharSequence to check, may be null
   * @return true if cs has content beyond whitespace
   * @since 3.0
   */
  def isNotBlank(cs: CharSequence): Boolean = !isBlank(cs)

  /** Returns true if the CharSequence is not null and has at least one character. */
  @inline
  def isNotEmpty(cs: CharSequence): Boolean = !(cs eq null) && cs.length > 0

  /** Joins the elements of the sequence with the specified delimiter.
   *
   * @param seq       the iterable to join
   * @param delimiter the delimiter to use between elements
   * @return the joined string
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

  /** Joins the given strings with the default comma delimiter.
   *
   * @param seq the strings to join
   * @return the joined string
   */
  def join(seq: String*): String = join(seq, DELIMITER)

  /** Joins the array of strings with the specified delimiter. The result has no delimiter at start or end.
   *
   * @param seq       the array of strings to join
   * @param delimiter the delimiter to use between elements
   * @return the joined string
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

  /** Keeps only unique words in the comma-separated string, preserving original order.
   *
   * @param keyString the comma-separated string to deduplicate
   * @return the string with duplicate words removed
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

  /** Left-pads a String with a specified character to the given size.
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
   * @param size    the target size to pad to
   * @param padChar the character to pad with
   * @return left-padded String or original if no padding needed, null if null input
   * @since 3.0
   */
  def leftPad(str: String, size: Int, padChar: Char): String = {
    if (str == null) return null
    val pads = size - str.length
    if (pads <= 0) return str
    repeat(padChar, pads).concat(str)
  }

  /** Right-pads a String with a specified character to the given size.
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
   * @param size    the target size to pad to
   * @param padChar the character to pad with
   * @return right-padded String or original if no padding needed, null if null input
   * @since 3.0
   */
  def rightPad(str: String, size: Int, padChar: Char): String = {
    if (str == null) return null
    val pads = size - str.length
    if (pads <= 0) return str
    str.concat(repeat(padChar, pads))
  }

  /** Merges two comma-separated strings, removing duplicate words.
   *
   * @param first  the first delimiter-separated string
   * @param second the second delimiter-separated string
   * @return the merged string with unique words
   */
  def mergeSeq(first: String, second: String): String = mergeSeq(first, second, DELIMITER)

  /** Merges two delimiter-separated strings into one, with duplicate words appearing only once.
   * If the first string starts with delimiter or the second ends with delimiter,
   * the merged result preserves those delimiters. Examples:
   * {{{
   * mergeSeq(",1,2,", "") = ",1,2,";
   * mergeSeq(",1,2,", null) = ",1,2,";
   * mergeSeq("1,2", "3") = "1,2,3";
   * mergeSeq("1,2", "3,") = "1,2,3,";
   * mergeSeq(",1,2", "3,") = ",1,2,3,";
   * mergeSeq(",1,2,", ",3,") = ",1,2,3,";
   * }}}
   *
   * @param first     the first delimiter-separated string
   * @param second    the second delimiter-separated string
   * @param delimiter the delimiter used to separate words
   * @return the merged string with unique words
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

  /** Removes all occurrences of the word from the comma-separated host string.
   *
   * @param host the delimiter-separated string to remove from
   * @param word the word to remove
   * @return the string with the word removed
   */
  def removeWord(host: String, word: String): String = removeWord(host, word, DELIMITER)

  /** Removes all occurrences of the word from the delimiter-separated host string.
   *
   * @param host      the string to remove from
   * @param word      the word to remove
   * @param delimiter the delimiter used to separate words
   * @return the string with the word removed
   */
  def removeWord(host: String, word: String, delimiter: String): String = {
    if host.indexOf(word) == -1 then host
    else
      split(host, delimiter).filterNot(_ == word).mkString(delimiter)
  }

  /** Returns a string of the specified character repeated to the given length.
   * {{{
   * repeat(0, 'e')  = ""
   * repeat(3, 'e')  = "eee"
   * repeat(-2, 'e') = ""
   * }}}
   *
   * @param ch     the character to repeat
   * @param repeat number of times to repeat, negative treated as zero
   * @return the repeated character string
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

  /** Repeats a String the given number of times to form a new String.
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
   * @param repeat number of times to repeat, negative treated as zero
   * @return the repeated string, null if null input
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

  /** Replaces all occurrences of a String within another String. Null references are no-op.
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
   * @param text         the text to search and replace in, may be null
   * @param searchString the String to search for, may be null
   * @param replacement  the String to replace with, may be null
   * @return the text with replacements applied, null if null input
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

  /** Splits the string by comma, semicolon, carriage return, newline, or space.
   *
   * @param target the string to split
   * @return an array of non-empty substrings
   */
  def split(target: String): Array[String] = {
    split(target, Array(',', ';', '\r', '\n', ' '))
  }

  /** Splits the text into an array using the specified separator character.
   * Null input returns null. Multiple adjacent separators produce empty string gaps.
   * {{{
   * split(null, *)         = null
   * split("", *)           = []
   * split("a.b.c", '.')    = ["a", "b", "c"]
   * split("a..b.c", '.')   = ["a", "b", "c"]
   * split("a:b:c", '.')    = ["a:b:c"]
   * split("a b c", ' ')    = ["a", "b", "c"]
   * }}}
   *
   * @param str           the string to split, may be null
   * @param separatorChar the separator character
   * @return array of substrings, or null if null input
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

  /** Adds non-empty trimmed substring from chars[start..end) to the buffer. */
  def addNonEmpty(buffer: mutable.Buffer[String], chars: Array[Char], start: Int, end: Int): Unit = {
    val rs = new String(chars, start, end - start).trim()
    if rs.nonEmpty then buffer.addOne(rs)
  }

  /** Splits the string using any of the characters in the separator array.
   *
   * @param target         the string to split
   * @param separatorChars the array of separator characters
   * @return an array of substrings
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

  /** Splits the text into an array using any character in the separator string.
   * Null input returns null. Null separatorChars splits on whitespace.
   * {{{
   * split(null, *)         = null
   * split("", *)           = []
   * split("abc def", null) = ["abc", "def"]
   * split("abc def", " ")  = ["abc", "def"]
   * split("abc  def", " ") = ["abc", "def"]
   * split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
   * }}}
   *
   * @param str            the string to split, may be null
   * @param separatorChars the separator characters, null for whitespace
   * @return array of substrings, or null if null input
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

  /** Parses a number sequence string like "1-2,3,4-9" into an array of integers.
   * Supports ranges (e.g. 1-5) and comma-separated values.
   *
   * @param numSeq the number sequence string to parse
   * @return an array of integers, or null if input is empty
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

  /** Splits the comma-separated string and converts each part to an integer. */
  def splitToInt(ids: String): Seq[Int] =
    if (isEmpty(ids)) List.empty else Numbers.toInt(split(ids, ',')).toIndexedSeq

  /** Splits the comma-separated string and converts each part to a long. */
  def splitToLong(ids: String): Seq[Long] = {
    if (isEmpty(ids)) List.empty else Numbers.toLong(split(ids, ',')).toIndexedSeq
  }

  /** Gets a substring from the specified String avoiding exceptions.
   * Negative positions count from the end. Zero-based. Returns "" if start >= end.
   * {{{
   * substring(null, *, *)    = null
   * substring("", *, *)      = ""
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
   * @param startIndex start position (negative = from end)
   * @param endIndex   end position exclusive (negative = from end)
   * @return the substring, null if null input
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

  /** Returns a new comma-separated string containing words in first but not in second.
   *
   * @param first  the first delimiter-separated string
   * @param second the second delimiter-separated string (words to subtract)
   * @return the result string, with leading and trailing delimiter if original had them
   */
  def subtractSeq(first: String, second: String): String = subtractSeq(first, second, DELIMITER)

  /** Returns a new delimiter-separated string with words from first minus words in second.
   * The result preserves leading/trailing delimiter if the original first string had them.
   *
   * @param first     the source string
   * @param second    the string whose words are to be subtracted
   * @param delimiter the delimiter used to separate words
   * @return the result string
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

  /** Converts camelCase to hyphenated lowercase (e.g. camelCase -> camel-case). */
  def unCamel(str: String): String = unCamel(str, '-', true)

  /** Converts camelCase to the specified separator style (e.g. camelCase with '_' -> camel_case).
   *
   * @param str       the camelCase string to convert
   * @param seperator the separator character to insert between words
   * @return the converted string
   */
  def unCamel(str: String, seperator: Char): String = unCamel(str, seperator, true)

  /** Converts camelCase to separated format (e.g. underscore lowercase).
   *
   * @param str       the camelCase string to convert
   * @param seperator the separator character to insert between words
   * @param lowercase whether to convert to lowercase
   * @return the converted string
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

  /** Gets the substring before the first occurrence of a separator. Separator not included.
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
   * @param str       the String to get substring from, may be null
   * @param separator the separator to search for, may be null
   * @return substring before first separator, null if null input
   * @since 2.0
   */
  def substringBefore(str: String, separator: String): String = {
    if (isEmpty(str) || separator == null) return str
    if (separator.isEmpty) return Empty
    val pos = str.indexOf(separator)
    if (pos == Index_not_found) return str

    str.substring(0, pos)
  }

  /** Gets the substring after the first occurrence of a separator. Separator not included.
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
   * @param str       the String to get substring from, may be null
   * @param separator the separator to search for, may be null
   * @return substring after first separator, empty if not found
   * @since 2.0
   */
  def substringAfter(str: String, separator: String): String = {
    if (isEmpty(str)) return str
    if (separator == null) return Empty
    val pos = str.indexOf(separator)
    if (pos == Index_not_found) return Empty
    str.substring(pos + separator.length)
  }

  /** Gets the String nested between the open and close strings. Returns first match only.
   * {{{
   * substringBetween("wx[b]yz", "[", "]") = "b"
   * substringBetween(null, *, *)          = null
   * substringBetween(*, null, *)          = null
   * substringBetween(*, *, null)          = null
   * substringBetween("", "", "")          = ""
   * substringBetween("yabcz", "y", "z")   = "abc"
   * substringBetween("yabczyabcz", "y", "z") = "abc"
   * }}}
   *
   * @param str   the String containing the substring, may be null
   * @param open  the String before the substring, may be null
   * @param close the String after the substring, may be null
   * @return the nested substring, null if no match
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

  /** Gets the substring before the last occurrence of a separator. Separator not included.
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
   * @param str       the String to get substring from, may be null
   * @param separator the separator to search for, may be null
   * @return substring before last separator
   * @since 3.0
   */
  def substringBeforeLast(str: String, separator: String): String = {
    if (isEmpty(str) || isEmpty(separator)) return str
    val pos = str.lastIndexOf(separator)
    if (pos == Index_not_found) return str
    str.substring(0, pos)
  }

  /** Gets the substring after the last occurrence of a separator. Separator not included.
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
   * @param str       the String to get substring from, may be null
   * @param separator the separator to search for, may be null
   * @return substring after last separator, empty if not found
   * @since 3.0
   */
  def substringAfterLast(str: String, separator: String): String = {
    if (isEmpty(str)) return str
    if (isEmpty(separator)) return Empty
    val pos = str.lastIndexOf(separator)
    if (pos == Index_not_found || pos == str.length - separator.length) return Empty
    str.substring(pos + separator.length)
  }

  /** Removes control characters (char <= 32) from both ends using `String#trim()`.
   * {{{
   * trim(null)          = null
   * trim("")            = ""
   * trim("     ")       = ""
   * trim("abc")         = "abc"
   * trim("    abc    ") = "abc"
   * }}}
   *
   * @param str the String to trim, may be null
   * @return the trimmed string, null if null input
   * @since 3.0
   */
  def trim(str: String): String =
    if (str == null) null else str.trim()

  /** Trims whitespace from the end of the string. Returns null for null input. */
  def trimEnd(str: String): String =
    if (str == null)
      null
    else {
      var len = str.length
      while (len > 0 && (str.charAt(len - 1) <= ' '))
        len -= 1
      if (len < str.length) str.substring(0, len) else str
    }

  /** Strips any of the specified characters from the end of a String.
   * Null input returns null. Null stripChars treats as whitespace.
   * {{{
   * stripEnd(null, *)          = null
   * stripEnd("", *)            = ""
   * stripEnd("abc", "")        = "abc"
   * stripEnd(*, null)          = *
   * stripEnd("  abcyx", "xyz") = "  abc"
   * }}}
   *
   * @param str        the String to strip, may be null
   * @param stripChars characters to remove, null for whitespace
   * @return the stripped String, null if null input
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

  /** Uncapitalizes a String changing the first letter to lowercase. No other letters changed.
   * {{{
   * uncapitalize(null)  = null
   * uncapitalize("")    = ""
   * uncapitalize("Cat") = "cat"
   * uncapitalize("CAT") = "cAT"
   * }}}
   *
   * @param str the String to uncapitalize, may be null
   * @return the uncapitalized String, null if null input
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

  /** Returns the passed-in CharSequence, or defaultStr if it is whitespace, empty, or null.
   *
   * @param str        the CharSequence to check
   * @param defaultStr the value to return when str is blank
   * @return str if not blank, otherwise defaultStr
   */
  def defaultIfBlank[T <: CharSequence](str: T, defaultStr: T): T =
    if (isBlank(str)) defaultStr else str

  /** Converts the string to lowercase. Returns null for null input. */
  def lowerCase(s: String): String =
    if (null == s) null else s.toLowerCase()

  /** Converts the string to uppercase. Returns null for null input. */
  def upperCase(s: String): String =
    if (null == s) null else s.toUpperCase()

  /** Abbreviates the string to the specified max width with ellipsis. */
  def abbreviate(str: String, maxWidth: Int): String =
    abbreviate(str, 0, maxWidth);

  /** Abbreviates the string from the given offset to max width with ellipsis.
   *
   * @param str      the string to abbreviate
   * @param offset   the starting offset
   * @param maxWidth the maximum width including ellipsis
   * @return the abbreviated string
   */
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

  /** Removes all occurrences of a character from the source string.
   * {{{
   * remove(null, *) = null
   * remove("", *) = ""
   * remove("queued", 'u') = "qeed"
   * remove("queued", 'z') = "queued"
   * }}}
   *
   * @param str    the source String to search, may be null
   * @param remove the character to remove
   * @return the string with the char removed, null if null input
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

  /** Formats the string using the given format and arguments (java.util.Formatter syntax).
   *
   * @param format the format string
   * @param args   the format arguments
   * @return the formatted string
   */
  def format(format: String, args: Any*): String = {
    new java.util.Formatter().format(format, args.toArray.asInstanceOf[Array[Object]]: _*).toString
  }

  /** Converts the string to an InputStream using UTF-8 encoding. */
  def asStream(contents: String): InputStream = {
    new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8))
  }

  /** Converts the string to an InputStream using the specified charset.
   *
   * @param contents the string content
   * @param charset  the charset for encoding
   * @return an InputStream of the encoded bytes
   */
  def asStream(contents: String, charset: Charset): InputStream = {
    new ByteArrayInputStream(contents.getBytes(charset))
  }
}
