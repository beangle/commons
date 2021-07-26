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

import java.util.Calendar._
import java.{ util => ju }

import org.beangle.commons.conversion.Converter
import org.beangle.commons.lang.Dates
import org.beangle.commons.lang.Numbers.toInt
import org.beangle.commons.lang.Strings._

/**
 * DateConverter
 *
 * @author chaostone
 * @since 3.2.0
 */
object String2DateConverter extends StringConverterFactory[String, ju.Date] {

  register(classOf[ju.Date], new DateConverter())

  register(classOf[java.sql.Date], new SqlDateConverter())

  private class DateConverter extends Converter[String, ju.Date] {

    override def apply(value: String): ju.Date = {
      if (isEmpty(value))
        return null
      val dateStr = value
      val times = split(dateStr, " ")
      var badformat = false
      var dateElems: Array[Int] = null
      if (contains(times(0), "-")) {
        dateElems = toInt(split(times(0), "-"))
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
        val gc = ju.Calendar.getInstance
        gc.set(YEAR, dateElems(0))
        gc.set(MONTH, dateElems(1) - 1)
        gc.set(DAY_OF_MONTH, dateElems(2))
        gc.set(MILLISECOND, 0)
        if (times.length > 1 && isNotBlank(times(1))) {
          val timeElems = split(times(1), ":")
          if (timeElems.length > 0) gc.set(HOUR_OF_DAY, toInt(timeElems(0)))
          if (timeElems.length > 1) gc.set(MINUTE, toInt(timeElems(1)))
          if (timeElems.length > 2) gc.set(SECOND, toInt(timeElems(2)))
        }
        gc.getTime
      }
    }
  }

  private class SqlDateConverter extends Converter[String, java.sql.Date] {
    override def apply(input: String): java.sql.Date =
      java.sql.Date.valueOf(Dates.normalize(input))
  }
}
