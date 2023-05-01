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

import org.beangle.commons.lang.Strings.{isEmpty, leftPad}
import org.beangle.commons.lang.time.HourMinute

import java.time.*
import java.util.Calendar

/**
 * Dates class.
 *
 * @author chaostone
 */
object Dates {


  def today: LocalDate = LocalDate.now()

  def now: LocalDateTime = LocalDateTime.now()

  def toHourMinute(value: String): HourMinute = {
    if isEmpty(value) then null else HourMinute.apply(value)
  }

  def toTime(value: String): LocalTime = {
    if isEmpty(value) then null else LocalTime.parse(value)
  }

  def toMonthDay(value: String): MonthDay = {
    if isEmpty(value) then null else MonthDay.parse(Dates.normalizeMonthDay(value))
  }

  def toYearMonth(value: String): YearMonth = {
    if isEmpty(value) then null else YearMonth.parse(Dates.nomalizeYearMonth(value))
  }

  def toDate(value: String): LocalDate = {
    if isEmpty(value) then null else LocalDate.parse(Dates.normalizeDate(value))
  }

  def toDateTime(value: String): LocalDateTime = {
    if isEmpty(value) then null else LocalDateTime.parse(normalizeDateTime(value))
  }

  def toZonedDateTime(value: String): ZonedDateTime = {
    if isEmpty(value) then null else ZonedDateTime.parse(Dates.normalizeDateTime(value))
  }

  def toOffsetateTime(value: String): OffsetDateTime = {
    if isEmpty(value) then null else OffsetDateTime.parse(Dates.normalizeDateTime(value))
  }

  def toInstant(value: String): Instant = {
    if isEmpty(value) then return null
    if value.endsWith("Z") then Instant.parse(value)
    else OffsetDateTime.parse(Dates.normalizeDateTime(value)).toInstant
  }


  /**
   * normalize.
   * change other formats to uniform one.
   * <p>
   * YYYYMMDD => YYYY-MM-DD
   * YYYY-M-D => YYYY-MM-DD
   * YYYY.MM.dd =>YYYY-MM-DD
   * </p>
   *
   * @param dateStr a String object.
   * @return a String object.
   */
  def normalizeDate(str: String): String = {
    val dateStr = if (str.contains(".")) Strings.replace(str, ".", "-") else str
    if (!dateStr.contains("-")) {
      val dateBuf = new StringBuilder(dateStr)
      dateBuf.insert("yyyyMM".length, '-')
      dateBuf.insert("yyyy".length, '-')
      dateBuf.toString
    } else if (dateStr.length >= 10) dateStr else if (dateStr.length < 8) throw new IllegalArgumentException() else {
      val value = dateStr.toCharArray
      val dayIndex = if (value(6) == '-') 7 else {
        if (value(7) == '-') 8 else -1
      }
      if (dayIndex < 0) throw new IllegalArgumentException()
      val sb = new StringBuilder(10)
      sb.appendAll(value, 0, 5)
      if (dayIndex - 5 < 3) sb.append('0').appendAll(value, 5, 2) else sb.appendAll(value, 5, 3)
      if (value.length - dayIndex < 2) sb.append('0').appendAll(value, dayIndex, 1) else sb.appendAll(value, dayIndex, 2)
      sb.toString
    }
  }

  /** Change DateTime Format
   *  - YYYY-MM-DD HH:mm into YYYY-MM-DDTHH:mm:00
   *  - YYYY-MM-DD HH:mm:ss into YYYY-MM-DDTHH:mm:ss
   */
  def normalizeDateTime(value: String): String = {
    val v = if (value.length == 16) value + ":00" else value
    Strings.replace(v, " ", "T")
  }

  /** Change YearMonth Format
   *  - YYYY.M into YYYY-0M
   *  - YYYY.MM into YYYY-MM
   *  - YYYY-M into YYYY-0M
   */
  def nomalizeYearMonth(ym: String): String = {
    val str = Strings.replace(ym, ".", "-")
    if (str.contains("-")) {
      val parts = splitDate(str)
      parts(0) + "-" + parts(1)
    } else {
      val parts = splitDate(str.substring(0, 4) + "-" + str.substring(4))
      parts(0) + "-" + parts(1)
    }
  }

  def normalizeMonthDay(md: String): String = {
    var str = Strings.replace(md, ".", "-")
    if str.startsWith("--") then str = str.substring(2)
    val parts = splitDate(str)
    "--" + parts(0) + "-" + parts(1)
  }


  private def splitDate(str: String): Array[String] = {
    val parts = Strings.split(str, "-")
    parts.indices foreach { i =>
      parts(i) = leftPad(parts(i), 2, '0')
    }
    parts
  }
}
