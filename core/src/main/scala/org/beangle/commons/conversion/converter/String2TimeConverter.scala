/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
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
package org.beangle.commons.conversion.converter

import org.beangle.commons.lang.Strings
import org.beangle.commons.conversion.Converter
import java.sql.Time

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

  override def apply(input: String): Time = {
    if (Strings.isEmpty(input)) null
    try {
      Time.valueOf(normalize(input))
    } catch {
      case e: Exception => null
    }
  }
  
  private def normalize(timeStr: String): String = {
    if (timeStr.length >= 8) timeStr
    else {
      val buf = new StringBuilder(timeStr)
      if (buf.length <= 5) buf.append("00")
      if (buf.charAt(2) != ':') buf.insert(2, ':')
      if (buf.charAt(5) != ':') buf.insert(5, ':')
      buf.toString
    }
  }
}
