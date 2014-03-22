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
package org.beangle.commons.lang

import java.{ util => ju }
import java.util.Calendar._
import java.sql.Date
import java.sql.Time
/**
 * <p>
 * Dates class.
 * </p>
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
    val cal = getInstance()
    cal.setTime(date)
    cal.set(HOUR_OF_DAY, time.getHours)
    cal.set(MINUTE, time.getMinutes)
    cal.set(SECOND, 0)
    cal.set(MILLISECOND, 0)
    cal.getTime()
  }
}
