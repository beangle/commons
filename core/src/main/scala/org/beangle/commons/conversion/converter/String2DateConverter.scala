/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
import org.beangle.commons.lang.Strings.{ substring, transformToInt, split, isEmpty, contains, isNotBlank }
import org.beangle.commons.lang.Numbers.toInt

/**
 * DateConverter
 *
 * @author chaostone
 * @since 3.2.0
 */
class String2DateConverter extends StringConverterFactory[String, Date] {

  register(classOf[Date], new DateConverter())

  register(classOf[java.sql.Date], new SqlDateConverter())

  private class DateConverter extends Converter[String, Date] {

    override def apply(value: String): Date = {
      if (isEmpty(value.asInstanceOf[String])) {
        return null
      }
      val dateStr = value.asInstanceOf[String]
      val times = split(dateStr, " ")
      var badformat = false
      var dateElems: Array[Int] = null
      if (contains(times(0), "-")) {
        dateElems = transformToInt(split(times(0), "-"))
        badformat = dateElems.length != 3
      } else {
        dateElems = new Array[Int](3)
        val yearIndex = "yyyy".length
        dateElems(0) = toInt(substring(times(0), 0, yearIndex))
        dateElems(1) = toInt(substring(times(0), yearIndex, yearIndex + 2))
        dateElems(2) = toInt(substring(times(0), yearIndex + 2, yearIndex + 4))
        badformat = (times(0).length != 8)
      }
      badformat ||= (dateElems(1) > 12 || dateElems(2) > 31)

      if (badformat) null
      else {
        val gc = Calendar.getInstance
        gc.set(Calendar.YEAR, dateElems(0))
        gc.set(Calendar.MONTH, dateElems(1) - 1)
        gc.set(Calendar.DAY_OF_MONTH, dateElems(2))
        if (times.length > 1 && isNotBlank(times(1))) {
          val timeElems = split(times(1), ":")
          if (timeElems.length > 0) gc.set(Calendar.HOUR_OF_DAY, toInt(timeElems(0)))
          if (timeElems.length > 1) gc.set(Calendar.MINUTE, toInt(timeElems(1)))
          if (timeElems.length > 2) gc.set(Calendar.SECOND, toInt(timeElems(2)))
        }
        gc.getTime
      }
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
