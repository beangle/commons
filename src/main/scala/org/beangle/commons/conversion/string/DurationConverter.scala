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

package org.beangle.commons.conversion.string

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Strings

import java.time.Duration

/** Converts string to java.time.Duration. Supports ISO-8601 and Chinese/English units (天/day, 时/hour, 分/minute, 秒/second). */
object DurationConverter extends Converter[String, Duration] {

  /** Parses a duration string to Duration.
   *
   * @param str the duration string (e.g. "1D", "2H30M", "1天2小时")
   * @return the parsed Duration
   */
  override def apply(str: String): Duration = {
    Duration.parse(normalize(str))
  }

  private[string] def normalize(str: String): String = {
    var d = str.trim().toUpperCase()
    d = Strings.replace(d, "天", "D")
    d = Strings.replace(d, "天", "D")
    d = Strings.replace(d, "日", "D")
    d = Strings.replace(d, "小时", "H")
    d = Strings.replace(d, "分钟", "M")

    d = Strings.replace(d, "DAYS", "D")
    d = Strings.replace(d, "DAY", "D")
    d = Strings.replace(d, "HOURS", "H")
    d = Strings.replace(d, "HOUR", "H")
    d = Strings.replace(d, "MINUTES", "M")
    d = Strings.replace(d, "MINUTE", "M")
    d = Strings.replace(d, "SECONDS", "S")
    d = Strings.replace(d, "SECOND", "S")

    d = Strings.replace(d, "时", "H")
    d = Strings.replace(d, "分", "M")
    d = Strings.replace(d, "秒", "S")

    d = Strings.replace(d, " ", "")
    if (Strings.contains(d, 'D') && !d.endsWith("D") && !Strings.contains(d, "DT")) {
      d = Strings.replace(d, "D", "DT")
    }
    if (!d.contains('P')) {
      d = "P" + d
    }
    if (!Strings.contains(d, 'T') && !d.endsWith("D")) {
      d = Strings.replace(d, "P", "PT")
    }
    d
  }

}
