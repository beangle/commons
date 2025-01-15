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

package org.beangle.commons.json

import org.beangle.commons.lang.Strings

import java.io.Reader

object JsonParser {
  def parse(s: String): Any = {
    val parser = new JsonParser(new java.io.StringReader(s))
    parser.parse()
  }

  def parseObject(s: String): JsonObject = {
    if (Strings.isBlank(s)) new JsonObject()
    else parse(s).asInstanceOf[JsonObject]
  }

  def parseArray(s: String): JsonArray = {
    if (Strings.isBlank(s)) new JsonArray()
    else parse(s).asInstanceOf[JsonArray]
  }
}

class JsonParser(reader: Reader) {
  private var index = 1 //current index in the buffer
  private var length = 0 // max length of buffer
  private val buffer = new Array[Char](1024)

  /** Parse text into a json object or value
   *
   * @return
   */
  def parse(): Any = {
    try {
      val c = this.readChar()
      c match {
        case '{' =>
          this.back()
          parseObject()
        case '[' =>
          this.back()
          parseArray()
        case _ => readValue(c)
      }
    } catch {
      case e: StackOverflowError => throw new RuntimeException("json array or object depth too large to process.", e)
    }
  }

  private def readChar(): Char = {
    while (true) {
      val c = this.next()
      if (c == 0 || c > ' ') return c
    }
    0
  }

  private def next(): Char = {
    if (this.index >= this.length) {
      var last: Char = 0
      if (length > 0) last = this.buffer(length - 1)
      val readLen = this.reader.read(this.buffer, 1, this.buffer.length - 1)
      this.length = if readLen < 0 then 0 else readLen + 1

      this.index = 1
      if this.length == 0 then return 0
      else this.buffer(0) = last
    }

    val c = this.buffer(this.index)
    this.index += 1
    c
  }

  private def readChars(n: Int): String = {
    if (n == 0) {
      return ""
    }
    val chars = new Array[Char](n)
    var pos = 0

    while (pos < n) {
      chars(pos) = this.next()
      if (this.end) {
        throw this.syntaxError("Substring bounds error")
      }
      pos += 1
    }
    new String(chars)
  }

  private def readValue(char: Char): Any = {
    var string: String = null
    char match {
      case '"' | '\'' =>
        return this.readString(char)
      case _ =>
    }
    var c = char
    val sb = new StringBuilder
    while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
      sb.append(c)
      c = this.next()
    }
    if (!this.end) this.back()
    string = sb.toString.trim
    if ("" == string) throw this.syntaxError("Missing value")
    stringToValue(string)
  }

  private def readString(quote: Char): String = {
    var c: Char = 0
    val sb = new StringBuilder()
    var break = false
    while (!break) {
      c = this.next()
      c match {
        case 0 | '\n' | '\r' =>
          throw this.syntaxError("Unterminated string. " +
            "Character with int code " + c + " is not allowed within a quoted string.")
        case '\\' =>
          c = this.next()
          c match {
            case 'b' => sb.append('\b')
            case 't' => sb.append('\t')
            case 'n' => sb.append('\n')
            case 'f' => sb.append('\f')
            case 'r' => sb.append('\r')
            case 'u' =>
              val next = this.readChars(4)
              try {
                sb.append(Integer.parseInt(next, 16).asInstanceOf[Char])
              } catch {
                case e: NumberFormatException =>
                  throw this.syntaxError("Illegal escape. " +
                    "\\u must be followed by a 4 digit hexadecimal number. \\" + next + " is not valid.")
              }
            case '"' | '\'' | '\\' | '/' => sb.append(c)
            case _ => throw this.syntaxError("Illegal escape.\\" + c + " is not valid.")
          }
        case _ => if c == quote then break = true else sb.append(c)
      }
    }
    sb.toString()
  }

  private def parseObject(): JsonObject = {
    val jo = new JsonObject
    var c: Char = 0
    var key: String = null
    if (this.readChar() != '{') throw this.syntaxError("A json object text must begin with '{'")

    while (true) {
      c = this.readChar()
      c match {
        case 0 => throw this.syntaxError("A Json object text must end with '}'")
        case '}' => return jo
        case _ => key = this.readValue(c).toString
      }
      c = this.readChar() // The key is followed by ':'.
      if (c != ':') throw this.syntaxError("Expected a ':' after a key")
      if (key != null) {
        val value = this.parse()
        if (value != null) jo.add(key, value)
      }
      // Pairs are separated by ','.
      this.readChar() match {
        case ';' => //ignore non-strict grammar
        case ',' =>
          if this.readChar() == '}' then return jo
          if (this.end) throw this.syntaxError("A json object text must end with '}'")
          this.back()
        case '}' => return jo
        case c => throw this.syntaxError(s"Expected a ',' or '}',but occurred $c")
      }
    }
    jo
  }

  private def parseArray(): JsonArray = {
    val ja = new JsonArray
    if (readChar() != '[') throw syntaxError("A jsonArray text must start with '['")
    var nextChar = readChar()
    if nextChar == 0 then throw syntaxError("Expected a ',' or ']'")
    if (nextChar != ']') {
      back()
      while (true) {
        if (readChar() == ',') {
          this.back()
          ja.add(Null)
        } else {
          this.back()
          ja.add(this.parse())
        }
        this.readChar() match {
          case 0 => throw syntaxError("Expected a ',' or ']'")
          case ',' =>
            nextChar = readChar()
            if nextChar == 0 then throw syntaxError("Expected a ',' or ']'")
            if nextChar == ']' || nextChar == ',' then return ja
            this.back()
          case ']' => return ja
          case _ => throw syntaxError("Expected a ',' or ']'")
        }
      }
    }
    ja
  }

  private def stringToValue(str: String): Any = {
    if ("" == str) return str
    if ("true".equalsIgnoreCase(str)) return true
    if ("false".equalsIgnoreCase(str)) return false
    if ("null".equalsIgnoreCase(str)) return Null
    val initial = str.charAt(0)
    if ((initial >= '0' && initial <= '9') || initial == '-') {
      val isDecimal = str.indexOf('.') > -1 || str.indexOf('e') > -1 || str.indexOf('E') > -1 || "-0".equals(str)
      try
        if isDecimal then java.lang.Double.valueOf(str) else java.lang.Long.valueOf(str)
      catch
        case exception: Exception => str
    } else {
      str
    }
  }

  private def end: Boolean = {
    this.length < 0
  }

  private def back(): Unit = {
    if (this.index > 0) {
      this.index -= 1
    } else {
      throw new RuntimeException("Stepping back two steps is not supported")
    }
  }

  private def syntaxError(message: String): RuntimeException = {
    if (this.index < this.length) {
      val len = Math.min(10, this.length - this.index)
      val c = new String(this.buffer.slice(this.index, this.index + len))
      throw new RuntimeException(message + " -- " + c)
    } else {
      throw new RuntimeException(message)
    }
  }
}
