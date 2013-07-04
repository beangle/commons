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
package org.beangle.commons.jdbc.dialect

import org.beangle.commons.lang.Strings

/**
 * User [a,b] or (a,b) or a,b to discribe jdbc version range.
 * a,b should not all empty.
 *
 * @author chaostone
 */
class Dbversion(version: String) {

  val containStart: Boolean = !version.startsWith("(")
  val containEnd: Boolean = !version.endsWith(")")
  var start: String = null
  var end: String = null

  var commaIndex: Int = version.indexOf(',');
  if (-1 == commaIndex) {
    start = version;
    end = version;
  } else {
    if ('[' == version.charAt(0) || '(' == version.charAt(0)) {
      start = version.substring(1, commaIndex);
    } else {
      start = version.substring(0, commaIndex);
    }
    if (']' == version.charAt(version.length() - 1) || ')' == version.charAt(version.length() - 1)) {
      end = version.substring(commaIndex + 1, version.length() - 1);
    } else {
      end = version.substring(commaIndex + 1);
    }

  }

  def contains(given: String): Boolean = {
    if (Strings.isNotEmpty(start)) {
      val rs: Int = start.compareTo(given);
      if ((!containStart && 0 == rs) || rs > 0) return false;
    }
    if (Strings.isNotEmpty(end)) {
      val rs: Int = end.compareTo(given);
      if ((!containEnd && 0 == rs) || rs < 0) return false;
    }
    return true;
  }
}
