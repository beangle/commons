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

/** Date and time parsing utilities.
 *
 * @author chaostone
 */
object Dates {

  /** Today's date. */
  def today: LocalDate = LocalDate.now()

  /** Current date-time. */
  def now: LocalDateTime = LocalDateTime.now()

  /** Parses string to HourMinute. */
  def toHourMinute(value: String): HourMinute = {
    if isEmpty(value) then null else HourMinute.apply(value)
  }

  /** Parses string to LocalTime. */
  def toTime(value: String): LocalTime = {
    if isEmpty(value) then null else LocalTime.parse(value)
  }

  /** Parses string to MonthDay. */
  def toMonthDay(value: String): MonthDay = {
    if isEmpty(value) then null else MonthDay.parse(Dates.normalizeMonthDay(value))
  }

  /** Parses string to YearMonth. */
  def toYearMonth(value: String): YearMonth = {
    if isEmpty(value) then null else YearMonth.parse(Dates.nomalizeYearMonth(value))
  }

  /** Parses string to LocalDate. */
  def toDate(value: String): LocalDate = {
    if isEmpty(value) then null else LocalDate.parse(Dates.normalizeDate(value))
  }

  /** Parses string to LocalDateTime. */
  def toDateTime(value: String): LocalDateTime = {
    if isEmpty(value) then null else LocalDateTime.parse(normalizeDateTime(value))
  }

  /** Parses string to ZonedDateTime. */
  def toZonedDateTime(value: String): ZonedDateTime = {
    if isEmpty(value) then null else ZonedDateTime.parse(Dates.normalizeDateTime(value))
  }

  /** Parses string to OffsetDateTime. */
  def toOffsetateTime(value: String): OffsetDateTime = {
    if isEmpty(value) then null else OffsetDateTime.parse(Dates.normalizeDateTime(value))
  }

  /** Parses string to Instant. */
  def toInstant(value: String): Instant = {
    if isEmpty(value) then return null
    if value.endsWith("Z") then Instant.parse(value)
    else if value.contains('+') then OffsetDateTime.parse(Dates.normalizeDateTime(value)).toInstant
    else {
      val text = Dates.normalizeDateTime(value)
      LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant
    }
  }

  /** Normalizes date string to YYYY-MM-DD. Handles YYYYMMDD, YYYY-M-D, YYYY.MM.dd.
   *
   * @param str the date string
   * @return normalized string
   */
  def normalizeDate(str: String): String = {
    var dateStr = if (str.contains(".")) Strings.replace(str, ".", "-") else str
    dateStr = Strings.replace(dateStr, "/", "-")
    if (!dateStr.contains("-")) {
      val dateBuf = new StringBuilder(dateStr)
      dateBuf.insert("yyyyMM".length, '-')
      dateBuf.insert("yyyy".length, '-')
      dateBuf.toString
    } else if (dateStr.length >= 10) {
      dateStr
    } else if (dateStr.length < 8) throw new IllegalArgumentException()
    else {
      val value = dateStr.toCharArray
      val dayIndex =
        if (value(6) == '-') 7 else {
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

  /** Normalizes date-time to ISO format (YYYY-MM-DDTHH:mm:ss).
   *
   * - YYYY-MM-DD HH:mm into YYYY-MM-DDTHH:mm:00
   * - YYYY-MM-DD HH:mm:ss into YYYY-MM-DDTHH:mm:ss
   *
   * @param value datetime string
   */
  def normalizeDateTime(value: String): String = {
    val v = Strings.replace(value, " ", "T")
    val sepIdx = v.indexOf('T')
    var hms: String = null
    var date: String = null
    if (sepIdx == -1) {
      hms = "00:00:00"
      date = v
    } else {
      hms = v.substring(sepIdx + 1)
      date = v.substring(0, sepIdx)
      val commaCount = Strings.count(hms, ':')
      if (hms.isEmpty) hms = "00:00:00"
      else if (commaCount == 0) {
        // Accept `H` and normalize to `HH:00:00`
        if hms.length == 1 then hms = leftPad(hms, 2, '0')
        hms += ":00:00"
      } else if (commaCount == 1) {
        // Accept `H:mm` and normalize to `HH:mm:00`
        val firstColon = hms.indexOf(':')
        val hourPart = hms.substring(0, firstColon)
        if hourPart.length == 1 then hms = leftPad(hourPart, 2, '0') + hms.substring(firstColon)
        hms += ":00"
      } else {
        // Accept `H:mm:ss` and normalize to `HH:mm:ss`
        val firstColon = hms.indexOf(':')
        val hourPart = hms.substring(0, firstColon)
        if hourPart.length == 1 then hms = leftPad(hourPart, 2, '0') + hms.substring(firstColon)
      }
    }
    normalizeDate(date) + "T" + hms
  }

  /** Normalizes YearMonth string to `YYYY-MM` (for `YearMonth.parse`).
   *
   * Supported examples:
   *  - `YYYY.M` into `YYYY-0M` (e.g. `2023.9` -> `2023-09`)
   *  - `YYYY.MM` into `YYYY-MM` (e.g. `2023.09` -> `2023-09`)
   *  - `YYYY-M` into `YYYY-0M` (e.g. `2023-9` -> `2023-09`)
   *  - also accepts `/` and fullwidth `／` as separators: `2023/09/01` -> `2023-09`
   *  - numeric formats: `YYYYMM` / `YYYYMMDD` (e.g. `20230901` -> `2023-09`)
   */
  def nomalizeYearMonth(ym: String): String = {
    val normalized = ym.trim.replaceAll("\\s+", "")
    var str = Strings.replace(Strings.replace(normalized, ".", "-"), "/", "-")
    str = Strings.replace(str, "／", "-")

    if (str.contains("-")) {
      val parts = splitDate(str)
      parts(0) + "-" + parts(1)
    } else {
      // Numeric formats: YYYYMM, YYYYMMDD (and a single-digit month like YYYYM).
      val digits = str.replaceAll("[^0-9]", "")
      require(digits.length >= 5, s"illegal yearMonth $ym,it should like yyyyMM or yyyyMMDD")
      val year = digits.substring(0, 4)
      val monthDigits = if (digits.length == 5) digits.substring(4) else digits.substring(4, 6)
      year + "-" + leftPad(monthDigits, 2, '0')
    }
  }

  /** Normalizes MonthDay string to --MM-DD. */
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
