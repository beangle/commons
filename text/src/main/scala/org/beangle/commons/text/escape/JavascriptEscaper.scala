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

package org.beangle.commons.text.escape

/** Escape javascript
  * borrow from freemarker.template.utility.StringUtil
  */
object JavascriptEscaper {

  private val NO_ESC = 0
  private val ESC_HEXA = 1
  private val ESC_BACKSLASH = 3

  def escape(s: String, json: Boolean): String = {
    val ln = s.length
    var sb: StringBuilder = null
    for (i <- 0 until ln) {
      val c = s.charAt(i)
      if (!(c > '>' && c < 0x7F && c != '\\') && c != ' ' && !(c >= 0xA0 && c < 0x2028)) { // skip common chars
        val escapeType: Int =
          if (c <= 0x1F) { // control chars range 1
            c match {
              case '\n' => 'n'
              case '\r' => 'r'
              case '\f' => 'f'
              case '\b' => 'b'
              case '\t' => 't'
              case _ => ESC_HEXA
            }
          }
          else if (c == '"') ESC_BACKSLASH
          else if (c == '\'') if json then NO_ESC else ESC_BACKSLASH
          else if (c == '\\') ESC_BACKSLASH
          else if (c == '/' && (i == 0 || s.charAt(i - 1) == '<')) ESC_BACKSLASH
          else if (c == '>') { // against "]]> and "-->"
            var dangerous = false
            if (i == 0) dangerous = true
            else {
              val prevC = s.charAt(i - 1)
              if (prevC == ']' || prevC == '-') if (i == 1) dangerous = true
              else {
                val prevPrevC = s.charAt(i - 2)
                dangerous = prevPrevC == prevC
              }
              else dangerous = false
            }
            if dangerous then (if json then ESC_HEXA else ESC_BACKSLASH) else NO_ESC
          } else {
            if (c == '<') { // against "<!"
              var dangerous = false
              if (i == ln - 1) dangerous = true
              else {
                val nextC = s.charAt(i + 1)
                dangerous = nextC == '!' || nextC == '?'
              }
              if dangerous then ESC_HEXA else NO_ESC
            } else if ((c >= 0x7F && c <= 0x9F) || (c == 0x2028 || c == 0x2029)) ESC_HEXA //control chars range 2 or UNICODE line terminators
            else NO_ESC
          }
        if (escapeType != NO_ESC) { // If needs escaping
          if (sb == null) {
            sb = new StringBuilder(ln + 6)
            sb.append(s.substring(0, i))
          }
          sb.append('\\')
          if (escapeType > 0x20) sb.append(escapeType.toChar)
          else if (escapeType == ESC_HEXA) if (!json && c < 0x100) {
            sb.append('x')
            sb.append(toHexDigit(c >> 4))
            sb.append(toHexDigit(c & 0xF))
          }
          else {
            sb.append('u')
            val cp = c
            sb.append(toHexDigit((cp >> 12) & 0xF))
            sb.append(toHexDigit((cp >> 8) & 0xF))
            sb.append(toHexDigit((cp >> 4) & 0xF))
            sb.append(toHexDigit(cp & 0xF))
          }
          else sb.append(c) // escapeType == ESC_BACKSLASH
        } else {
          if (sb != null) sb.append(c)
        }
      } else {
        if (sb != null) sb.append(c)
      }
    }
    // for each characters}
    if (sb == null) s else sb.toString
  }

  private def toHexDigit(d: Int): Char = (if d < 0xA then d + '0' else d - 0xA + 'A').asInstanceOf[Char]

}
