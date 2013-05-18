/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.text.regex

import java.util.regex.Pattern
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import AntPathPattern._

object AntPathPattern {

  def `match`(pattern: String, path: String): Boolean = {
    new AntPathPattern(pattern).`match`(path)
  }
}

/**
 * AntPattern implementation for Ant-style path patterns. Examples are provided below.
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
class AntPathPattern(antstring: String) {

  var antpattern: String = antstring

  var pattern: Pattern = Pattern.compile(preprocess(antstring))

  /**
   * translate ant string to regex string
   */
  private def preprocess(ant: String): String = {
    val sb = new StringBuilder()
    val length = antstring.length
    var i = 0
    while (i < length) {
      val c = antstring.charAt(i)
      var substr = String.valueOf(c)
      if (c == '.') substr = "\\." else if (c == '?') substr = "." else if (c == '*') {
        if (i + 1 < length) {
          val next1 = antstring.charAt(i + 1)
          if (next1 == '*') {
            i += 1
            var next2 = '\n'
            if (i + 1 < length) next2 = antstring.charAt(i + 1)
            if (next2 == '/') {
              i += 1
              substr = "(.*/)*"
            } else {
              substr = "(.*)"
            }
          } else substr = "([^/]*?)"
        } else {
          substr = "([^/]*?)"
        }
      }
      sb.append(substr)
    }
    sb.toString
  }

  override def hashCode(): Int = antpattern.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case obj: AntPathPattern => Objects.==(antpattern, obj.antpattern)
    case _ => false
  }

  def `match`(path: String): Boolean = pattern.matcher(path).matches()

  override def toString(): String = {
    Strings.concat("ant:[", antpattern, "] regex:[", pattern.toString, "]")
  }
}
