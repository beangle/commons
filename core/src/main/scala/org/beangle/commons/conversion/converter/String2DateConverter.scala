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
package org.beangle.commons.conversion.converter

import java.util.Date
import java.util.Calendar
import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Numbers

/**
 * <p>
 * DateConverter class.
 * </p>
 *
 * @author chaostone
 * @since 3.2.0
 * @version $Id: $
 */
class String2DateConverter extends StringConverterFactory[String, Date] {

  register(classOf[Date], new DateConverter())

  register(classOf[java.sql.Date], new SqlDateConverter())

  private class DateConverter extends Converter[String, Date] {

    override def apply(value: String): Date = {
      if (Strings.isEmpty(value.asInstanceOf[String])) {
        return null
      }
      val dateStr = value.asInstanceOf[String]
      val times = Strings.split(dateStr, " ")
      var dateElems: Array[String] = null
      if (Strings.contains(times(0), "-")) {
        dateElems = Strings.split(times(0), "-")
      } else {
        dateElems = new Array[String](3)
        val yearIndex = "yyyy".length
        dateElems(0) = Strings.substring(times(0), 0, yearIndex)
        dateElems(1) = Strings.substring(times(0), yearIndex, yearIndex + 2)
        dateElems(2) = Strings.substring(times(0), yearIndex + 2, yearIndex + 4)
      }
      val gc = Calendar.getInstance
      gc.set(Calendar.YEAR, Numbers.toInt(dateElems(0)))
      gc.set(Calendar.MONTH, Numbers.toInt(dateElems(1)) - 1)
      gc.set(Calendar.DAY_OF_MONTH, Numbers.toInt(dateElems(2)))
      if (times.length > 1 && Strings.isNotBlank(times(1))) {
        val timeElems = Strings.split(times(1), ":")
        if (timeElems.length > 0) gc.set(Calendar.HOUR_OF_DAY, Numbers.toInt(timeElems(0)))
        if (timeElems.length > 1) gc.set(Calendar.MINUTE, Numbers.toInt(timeElems(1)))
        if (timeElems.length > 2) gc.set(Calendar.SECOND, Numbers.toInt(timeElems(2)))
      }
      gc.getTime
    }
  }

  private class SqlDateConverter extends Converter[String, java.sql.Date] {

    override def apply(input: String): java.sql.Date = java.sql.Date.valueOf(normalize(input))
  }

  /**
   * <p>
   * normalize.
   * </p>
   *
   * @param dateStr a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  def normalize(dateStr: String): String = {
    if (!dateStr.contains("-")) {
      val dateBuf = new StringBuilder(dateStr)
      dateBuf.insert("yyyyMM".length, '-')
      dateBuf.insert("yyyy".length, '-')
      dateBuf.toString
    } else {
      if (dateStr.length >= 10) dateStr else if (dateStr.length < 8) throw new IllegalArgumentException() else {
        val value = dateStr.toCharArray()
        val dayIndex = if (value(6) == '-') 7 else { if (value(7) == '-') 8 else -1 }
        if (dayIndex < 0) throw new IllegalArgumentException()
        val sb = new StringBuilder(10)
        sb.appendAll(value, 0, 5)
        if (dayIndex - 5 < 3) sb.append('0').appendAll(value, 5, 2) else sb.appendAll(value, 5, 3)
        if (value.length - dayIndex < 2) sb.append('0').appendAll(value, dayIndex, 1) else sb.appendAll(value, dayIndex, 2)
        sb.toString
      }
    }
  }

}
