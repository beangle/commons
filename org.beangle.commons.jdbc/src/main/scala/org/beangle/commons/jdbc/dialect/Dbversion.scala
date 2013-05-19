/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
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
