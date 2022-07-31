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

package org.beangle.commons.lang

import java.time.{ LocalDate, LocalDateTime, LocalTime }
import java.util.Calendar

/**
 * Dates class.
 *
 * @author chaostone
 */
object Dates {

  def today: LocalDate =
    LocalDate.now()

  def now: LocalDateTime =
    LocalDateTime.now()

  def toDate(cal: Calendar): LocalDate =
    LocalDate.from(Calendar.getInstance.toInstant)

  def join(date: LocalDate, time: LocalTime): LocalDateTime =
    LocalDateTime.of(date, time)

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
    } else if (dateStr.length >= 10) dateStr else if (dateStr.length < 8) throw new IllegalArgumentException() else {
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
