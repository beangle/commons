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

/** Character utilities. */
object Chars {

  /** Checks whether the character is ASCII 7 bit alphabetic.
   *
   * <pre>
   * isAsciiAlpha('a')  = true
   * isAsciiAlpha('A')  = true
   * isAsciiAlpha('3')  = false
   * isAsciiAlpha('-')  = false
   * isAsciiAlpha('\n') = false
   * isAsciiAlpha('&copy;') = false
   * </pre>
   *
   * @param ch the character to check
   * @return true if between 65 and 90 or 97 and 122 inclusive
   * @since 3.0
   */
  def isAsciiAlpha(ch: Char): Boolean = {
    (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
  }

  /** Returns true if char is '0'-'9'. */
  def isNumber(char: Char): Boolean = {
    '0' <= char && char <= '9'
  }

  /** Returns display length (ASCII=1, others=2). */
  def charLength(str: String): Int = {
    val chars = str.toCharArray
    var l = 0
    chars.indices foreach { i =>
      l += (if chars(i) <= 127 then 1 else 2)
    }
    l
  }
}
