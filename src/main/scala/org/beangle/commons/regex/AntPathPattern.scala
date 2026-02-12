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

package org.beangle.commons.regex

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.regex.AntPathPattern.DefaultPattern

import java.util.regex.Pattern

/** Ant-style path pattern matching. */
object AntPathPattern {

  /** Returns true if the path fully matches the Ant-style pattern.
   *
   * @param pattern the Ant pattern
   * @param path    the path to match
   * @return true if full match
   */
  def matches(pattern: String, path: String): Boolean = new AntPathPattern(pattern).matches(path)

  /** Returns true if the path is a prefix match of the pattern.
   *
   * @param pattern the Ant pattern
   * @param path    the path to match
   * @return true if prefix match
   */
  def matchStart(pattern: String, path: String): Boolean = new AntPathPattern(pattern).matchStart(path)

  /** Returns true if the path contains Ant wildcards (* or ?).
   *
   * @param path the path to check
   * @return true if contains wildcards
   */
  def isPattern(path: String): Boolean = {
    (path.indexOf('*') != -1 || path.indexOf('?') != -1)
  }

  //? or /** or **/ or * or { (?: {[^/]+?} | [^/{}] | \\[{}] )+? }
  private val DefaultPattern = Pattern.compile("""\?|/\*\*|\*\*/|\*|\{((?:\{[^/]+?\}|[^/{}]|\\[{}])+?)\}""")
}

/** AntPattern implementation for Ant-style path patterns. Examples are provided below.
 *
 * Part of this mapping code has been kindly borrowed from [Apache Ant](http://ant.apache.org).
 *
 * The mapping matches URLs using the following rules:
 *
 * - `?` matches one character
 * - `*` matches zero or more characters
 * - `**` matches zero or more 'directories' in a path
 *
 * Some examples:
 *
 * - com/t?st.jsp - matches com/test.jsp` but also `com/tast.jsp` or `com/txst.jsp`
 * - com/&#42;sp - matches all `.jsp` files in the `com` directory
 * - org/beangle/&#42;&#42;/&#42;.jsp - matches all `.jsp` files underneath
 * - com/&#42;&#42;/test.jsp - matches all `test.jsp` files underneath the `com` path the `org/beangle` path
 * - org/&#42;&#42;/servlet/bla.jsp - matches `org/beangle/servlet/bla.jsp` but also  org/beangle/testing/servlet/bla.jsp and `org/servlet/bla.jsp`
 *
 * @author chaostone
 * @since 3.1.0
 */
class AntPathPattern(val text: String) {

  private var pattern: Pattern = _
  private var exactMatch: Boolean = _

  /** Variable names captured from {name} or {name:regex} in pattern. */
  var variables: List[String] = List.empty

  preprocess(text, false)

  /** Translates Ant-style pattern to regex and compiles it.
   *
   * @param pattern       Ant pattern string
   * @param caseSensitive whether matching is case-sensitive
   */
  def preprocess(pattern: String, caseSensitive: Boolean): Unit = {
    val builder = new StringBuilder()
    val matcher = DefaultPattern.matcher(pattern)
    var end = 0
    val vars = Collections.newBuffer[String]
    while (matcher.find()) {
      builder.append(quote(pattern, end, matcher.start()))
      val matched = matcher.group()
      if ("?" == matched) {
        builder.append('.')
      } else if ("**/" == matched || "/**" == matched) {
        builder.append(".*")
      } else if ("*" == matched) {
        builder.append("[^/]*")
      } else if (matched.startsWith("{") && matched.endsWith("}")) {
        val colonIdx = matched.indexOf(':')
        if (colonIdx == -1) {
          if isRegex(matched) then
            builder.append(s"(${matched.substring(1, matched.length - 1)})")
          else
            builder.append("((?s).*)")
            vars.addOne(matcher.group(1))
        } else {
          val variablePattern = matched.substring(colonIdx + 1, matched.length() - 1)
          builder.append('(').append(variablePattern).append(')')
          val variableName = matched.substring(1, colonIdx)
          vars.addOne(variableName)
        }
      }
      end = matcher.end();
    }
    this.variables = vars.toList
    if (end == 0) {
      this.exactMatch = true
      this.pattern = null
    } else {
      this.exactMatch = false
      builder.append(quote(pattern, end, pattern.length()))
      this.pattern = Pattern.compile(builder.toString(),
        Pattern.DOTALL | (if caseSensitive then 0 else Pattern.CASE_INSENSITIVE))
    }
  }

  private def isRegex(p: String): Boolean = {
    p.indexOf('*') > 0 || p.indexOf('.') > 0 || p.indexOf('+') > 0 || p.indexOf('[') > 0
  }

  override def hashCode(): Int = text.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case obj: AntPathPattern => text == obj.text
    case _ => false
  }

  /** Returns true if the path fully matches this pattern.
   *
   * @param path the path to match
   * @return true if full match
   */
  def matches(path: String): Boolean = {
    if exactMatch then text == path else pattern.matcher(path).matches()
  }

  /** Returns true if the path is a prefix match of this pattern.
   *
   * @param path the path to match
   * @return true if prefix match
   */
  def matchStart(path: String): Boolean = {
    if exactMatch then path.startsWith(text)
    else
      val m = pattern.matcher(path)
      m.matches()
      m.hitEnd()
  }

  private def quote(s: String, start: Int, end: Int): String = {
    if start == end then ""
    else Pattern.quote(s.substring(start, end));
  }

  override def toString: String = {
    if null == pattern then s"ant:[$text]"
    else Strings.concat("ant:[", text, "] regex:[", pattern.toString, "]")
  }
}
