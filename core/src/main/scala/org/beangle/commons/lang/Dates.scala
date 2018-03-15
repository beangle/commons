/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import java.{ util => ju }
import java.util.Calendar._
import java.sql.Date
import java.sql.Time
/**
 * Dates class.
 *
 * @author chaostone
 */
object Dates {

  /**
   * Roll Minutes.
   */
  def rollMinutes(date: ju.Date, mount: Int): ju.Date = new ju.Date(date.getTime + mount * 60 * 1000)

  def today: java.sql.Date = toDate(ju.Calendar.getInstance())

  def now: ju.Date = new ju.Date()

  def toDate(cal: ju.Calendar): Date = {
    val cloned = getInstance()
    cloned.set(HOUR_OF_DAY, 0)
    cloned.set(MINUTE, 0)
    cloned.set(SECOND, 0)
    cloned.set(MILLISECOND, 0)
    cloned.set(YEAR, cal.get(YEAR))
    cloned.set(MONTH, cal.get(MONTH))
    cloned.set(DAY_OF_MONTH, cal.get(DAY_OF_MONTH))
    new java.sql.Date(cloned.getTimeInMillis())
  }

  def toDate(date: ju.Date): Date = toDate(toCalendar(date))

  def toCalendar(date: ju.Date): ju.Calendar = {
    val cal = ju.Calendar.getInstance
    cal.setTime(date)
    cal
  }

  def toCalendar(dateStr: String): ju.Calendar = {
    val cal = getInstance()
    cal.setTime(java.sql.Date.valueOf(dateStr))
    cal
  }

  def join(date: Date, time: Time): ju.Date = {
    val cal = getInstance
    val timeCal = getInstance
    cal.setTime(date)
    timeCal.setTime(time)

    cal.set(HOUR_OF_DAY, timeCal.get(HOUR_OF_DAY))
    cal.set(MINUTE, timeCal.get(MINUTE))
    cal.set(SECOND, timeCal.get(SECOND))
    cal.set(MILLISECOND, 0)
    cal.getTime()
  }

  /**
   * normalize.
   * change other formats to uniform one.
   * <p>
   *    YYYYMMDD => YYYY-MM-DD
   *    YYYY-M-D => YYYY-MM-DD
   *    YYYY.MM.dd =>YYYY-MM-DD
   * </p>
   * @param dateStr a String object.
   * @return a String object.
   */
  def normalize(str: String): String = {
    val dateStr = if (str.contains(".")) Strings.replace(str, ".", "-") else str
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
