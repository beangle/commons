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

package org.beangle.commons.conversion.converter

import java.sql.Time

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Strings

/**
 * Convert String to Time.
 * <p>
 * Convert HH:mm:ss to java.sql.Time<br>
 * Convert HH:mm to java.sql.Time<br>
 *
 * @author chaostone
 * @since 4.0.3
 */
object String2TimeConverter extends Converter[String, Time] {

  override def apply(input: String): Time =
    if (Strings.isEmpty(input))
      null
    else
      try
        Time.valueOf(normalize(input))
      catch {
        case e: Exception => null
      }

  private def normalize(timeStr: String): String =
    if (timeStr.length >= 8) timeStr
    else {
      val buf = new StringBuilder(timeStr)
      if (buf.length <= 5) buf.append("00")
      if (buf.charAt(2) != ':') buf.insert(2, ':')
      if (buf.charAt(5) != ':') buf.insert(5, ':')
      buf.toString
    }
}
