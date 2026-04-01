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

/** Escapes XML special characters. */
object XmlEscaper {
  private val textTargets = Map('<' -> "&lt;", '>' -> "&gt;", '&' -> "&amp;")
  private val targets = textTargets ++ Map('"' -> "&quot;", '\'' -> "&apos;")
  private val entities = Map("lt" -> '<', "gt" -> '>', "amp" -> '&', "quot" -> '"', "apos" -> '\'')

  /** Escapes XML special characters including quotes. For attribute values.
   *
   * @param str the string to escape
   * @return the escaped string
   */
  def escape(str: String): String = {
    escape(str, targets)
  }

  /** Escapes XML text content (no quotes). For element text.
   *
   * @param str the string to escape
   * @return the escaped string
   */
  def escapeText(str: String): String = {
    escape(str, textTargets)
  }

  private def escape(str: String, esc: Map[Char, String]): String = {
    val ln = str.length
    var sb: StringBuilder = null
    val keys = esc.keySet
    (0 until ln).find(i => keys.contains(str.charAt(i))) match {
      case Some(firstMatchIdx) =>
        val sb = new StringBuilder(str.substring(0, firstMatchIdx))
        (firstMatchIdx until ln) foreach { i =>
          val c = str.charAt(i)
          sb.append(esc.getOrElse(c, c))
        }
        sb.mkString
      case None => str
    }
  }

  /** Unescapes XML entities including quotes. Reverse of [[escape]]. */
  def unescape(str: String): String = {
    val ln = str.length
    val firstAmp = str.indexOf('&')
    if firstAmp < 0 then return str

    val sb = new StringBuilder(str.substring(0, firstAmp))
    var i = firstAmp
    while i < ln do
      val c = str.charAt(i)
      if c != '&' then
        sb.append(c)
        i += 1
      else
        val semi = str.indexOf(';', i + 1)
        if semi < 0 then
          sb.append(c)
          i += 1
        else
          val name = str.substring(i + 1, semi)
          decodeEntity(name) match
            case Some(decoded) =>
              sb.append(decoded)
              i = semi + 1
            case None =>
              sb.append('&')
              i += 1
    sb.toString
  }

  private def decodeEntity(name: String): Option[Char] = {
    if name.startsWith("#x") || name.startsWith("#X") then
      parseCodePoint(name.substring(2), 16)
    else if name.startsWith("#") then
      parseCodePoint(name.substring(1), 10)
    else entities.get(name)
  }

  private def parseCodePoint(s: String, radix: Int): Option[Char] = {
    try
      val n = Integer.parseInt(s, radix)
      if n >= Char.MinValue && n <= Char.MaxValue then Some(n.toChar) else None
    catch
      case _: NumberFormatException => None
  }
}
