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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class XmlEscaperTest extends AnyFunSpec, Matchers {

  describe("XmlEscaper.escape") {
    it("escapes angle brackets, ampersand, and quotes for attribute-style content") {
      XmlEscaper.escape("""RSA key RSA key <expired="2019-09-09">""") should be(
        """RSA key RSA key &lt;expired=&quot;2019-09-09&quot;&gt;"""
      )
    }

    it("escapes ampersand before other special characters") {
      XmlEscaper.escape("a & b < c") should be("a &amp; b &lt; c")
    }

    it("escapes single quote as apos") {
      XmlEscaper.escape("it's") should be("it&apos;s")
    }

    it("escapes double quotes inside string") {
      XmlEscaper.escape("say \"hi\"") should be("say &quot;hi&quot;")
    }
  }

  describe("XmlEscaper.escapeText") {
    it("escapes lt, gt, amp only (not quotes)") {
      XmlEscaper.escapeText("1 < 2 && 3 > 0") should be("1 &lt; 2 &amp;&amp; 3 &gt; 0")
    }

    it("does not escape double or single quotes in text mode") {
      XmlEscaper.escapeText("""He said "ok" and 'yes'""") should be("""He said "ok" and 'yes'""")
    }
  }

  describe("XmlEscaper.unescape") {
    it("reverses named entities") {
      XmlEscaper.unescape("&lt;p&gt; &amp; &quot;x&quot; &apos;y&apos;") should be(
        """<p> & "x" 'y'"""
      )
    }

    it("decodes decimal numeric character references") {
      XmlEscaper.unescape("&#1212;") should be(1212.toChar.toString)
      XmlEscaper.unescape("&#34;quote") should be("\"quote")
      XmlEscaper.unescape("&#65;") should be("A")
    }

    it("decodes hexadecimal numeric character references") {
      XmlEscaper.unescape("&#x41;") should be("A")
      XmlEscaper.unescape("&#X61;") should be("a")
    }

    it("leaves incomplete or unknown entities as-is after partial decode") {
      XmlEscaper.unescape("no entity here") should be("no entity here")
      XmlEscaper.unescape("&") should be("&")
      XmlEscaper.unescape("&amp") should be("&amp")
    }

    it("round-trips escape then unescape for typical attribute fragment") {
      val raw = """<tag attr="a & b" c='d'>"""
      XmlEscaper.unescape(XmlEscaper.escape(raw)) should be(raw)
    }

    it("round-trips escapeText then unescape for text-only specials") {
      val raw = "2 < 3 && 4 > 1"
      XmlEscaper.unescape(XmlEscaper.escapeText(raw)) should be(raw)
    }
  }

}
