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

object AntPathPattern {

  def matches(pattern: String, path: String): Boolean = new AntPathPattern(pattern).matches(path)

  def matchStart(pattern: String, path: String): Boolean = new AntPathPattern(pattern).matchStart(path)

  def isPattern(path: String): Boolean = {
    (path.indexOf('*') != -1 || path.indexOf('?') != -1)
  }

  //? or /** or **/ or * or { (?: {[^/]+?} | [^/{}] | \\[{}] )+? }
  private val DefaultPattern = Pattern.compile("""\?|/\*\*|\*\*/|\*|\{((?:\{[^/]+?\}|[^/{}]|\\[{}])+?)\}""")
}

/** AntPattern implementation for Ant-style path patterns. Examples are provided below.
  * <p>
  * Part of this mapping code has been kindly borrowed from <a href="http://ant.apache.org">Apache
  * Ant</a>.
  * <p>
  * The mapping matches URLs using the following rules:<br>
  * <ul>
  * <li>? matches one character</li>
  * <li>* matches zero or more characters</li>
  * <li>** matches zero or more 'directories' in a path</li>
  * </ul>
  * <p>
  * Some examples:<br>
  * <ul>
  * <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
  * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li>
  * <li><code>com/&#42;sp</code> - matches all <code>.jsp</code> files in the <code>com</code> directory</li>
  * <li><code>org/beangle/&#42;&#42;/&#42;.jsp</code> - matches all <code>.jsp</code> files underneath
  * <li><code>com/&#42;&#42;/test.jsp</code> - matches all <code>test.jsp</code> files underneath the <code>com</code> path</li>
  * the <code>org/beangle</code> path</li>
  * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches
  * <code>org/beangle/servlet/bla.jsp</code> but also
  * <code>org/beangle/testing/servlet/bla.jsp</code> and <code>org/servlet/bla.jsp</code></li>
  * </ul>
  *
  * @author chaostone
  * @since 3.1.0
  */
class AntPathPattern(val text: String) {

  private var pattern: Pattern = _
  private var exactMatch: Boolean = _

  var variables: List[String] = List.empty

  preprocess(text, false)

  /** translate ant string to regex string
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

  def matches(path: String): Boolean = {
    if exactMatch then text == path else pattern.matcher(path).matches()
  }

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
    Strings.concat("ant:[", text, "] regex:[", pattern.toString, "]")
  }
}
