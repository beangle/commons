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

package org.beangle.commons.codec.net

import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.codec.net.BCoder.*
import org.beangle.commons.codec.{Decoder, Encoder}
import org.beangle.commons.lang.Charsets

import java.nio.charset.Charset

/** RFC 1522 MIME encoding (B) decoder/encoder factory. */
object BCoder {

  /** Separator */
  protected val Sep = '?'
  protected val Prefix = "=?"
  protected val Postfix = "?="
}

/** Identical to the Base64 encoding defined by <a href="http://www.ietf.org/rfc/rfc1521.txt">RFC
 * 1521</a> and allows a character set to be specified.
 * <p>
 * <a href="http://www.ietf.org/rfc/rfc1522.txt">RFC 1522</a> describes techniques to allow the
 * encoding of non-ASCII text in various portions of a RFC 822 [2] message header, in a manner which
 * is unlikely to confuse existing message handling software.
 * </p>
 *
 * @see <a href="http://www.ietf.org/rfc/rfc1522.txt">MIME (Multipurpose Internet Mail Extensions)
 *      Part Two: Message Header Extensions for Non-ASCII Text</a>
 * @author chaostone
 * @since 3.2.0
 */
class BCoder(val charset: Charset = Charsets.UTF_8) extends Encoder[String, String] with Decoder[String, String] {

  protected def encoding: String = "B"

  /** Encodes a string into its Base64 form using the default charset. Unsafe characters are escaped.
   *
   * @param value string to convert to Base64 form
   * @return Base64 string
   */
  def encode(value: String): String = {
    if value == null then null
    else
      val buffer = new StringBuilder()
      buffer.append(Prefix)
      buffer.append(charset)
      buffer.append(Sep)
      buffer.append(encoding)
      buffer.append(Sep)
      buffer.append(new String(Base64.encode(value.getBytes(charset))))
      buffer.append(Postfix)
      buffer.toString
  }

  /** Decodes a Base64 string into its original form. Escaped characters are converted back to their
   * original
   * representation.
   *
   * @param text Base64 string to convert into its original form
   * @return original string
   */
  def decode(text: String): String = {
    if (text == null)
      return null
    if ((!text.startsWith(Prefix)) || (!text.endsWith(Postfix))) throw new IllegalArgumentException("RFC 1522 violation: malformed encoded content")
    val terminator = text.length - 2
    var from = 2
    var to = text.indexOf(Sep, from)
    if (to == terminator) throw new IllegalArgumentException("RFC 1522 violation: charset token not found")
    val charset = text.substring(from, to)
    if (charset == "") throw new IllegalArgumentException("RFC 1522 violation: charset not specified")
    from = to + 1
    to = text.indexOf(Sep, from)
    if (to == terminator) throw new IllegalArgumentException("RFC 1522 violation: encoding token not found")
    val ec = text.substring(from, to)
    if (!encoding.equalsIgnoreCase(ec)) throw new IllegalArgumentException("This codec cannot decode " + ec + " encoded content")
    from = to + 1
    to = text.indexOf(Sep, from)
    new String(Base64.decode(text.substring(from, to)), charset)
  }
}
